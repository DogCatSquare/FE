package com.example.dogcatsquare.ui.community

import PostApiService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.api.CommentApiService
import com.example.dogcatsquare.data.model.community.Comment
import com.example.dogcatsquare.data.model.community.CommentListResponse
import com.example.dogcatsquare.data.model.community.CommentRequest
import com.example.dogcatsquare.data.model.community.CommentResponse
import com.example.dogcatsquare.data.model.community.CommonResponse
import com.example.dogcatsquare.data.model.community.LikeResponse
import com.example.dogcatsquare.data.model.community.PostDetailResponse
import com.example.dogcatsquare.data.model.community.Reply
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.ActivityPostDetailBinding
import com.example.dogcatsquare.ui.viewmodel.PostViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.SharedPreferences

class PostDetailActivity : AppCompatActivity(), CommentActionListener {

    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var commentAdapter: CommentsAdapter

    private val currentUserId: Long = 1L // TODO: 실제 로그인 사용자 ID로 교체
    private val postViewModel: PostViewModel by viewModels()

    private var postId: Int = -1
    private var isLiked: Boolean = false // !! 사용 제거
    private var like_count: Int = 0

    private val commentDatas = ArrayList<com.example.dogcatsquare.data.model.community.Comment>()
    private var videoUrl: String? = null

    private lateinit var likePref: SharedPreferences

    private fun getToken(): String? {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getString("token", null)
    }

    private fun getUserId(): Int? {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("userId", -1).takeIf { it != -1 }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        likePref = getSharedPreferences("like_prefs", Context.MODE_PRIVATE)

        // postId 로드 & 검증
        postId = intent.getIntExtra("postId", -1)
        if (postId == -1) {
            Toast.makeText(this, "게시글 ID가 전달되지 않았습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 로컬 좋아요 상태 복원
        isLiked = likePref.getBoolean("liked_$postId", false)
        setLikeButtonState(isLiked)

        // ViewModel 상태 관찰
        postViewModel.likedPosts.observe(this) { likedMap ->
            likedMap[postId]?.let { liked ->
                isLiked = liked
                setLikeButtonState(liked)
            }
        }

        // 뒤로가기 (중복 방지)
        binding.ivBack.setOnClickListener { finish() }

        // 댓글 리스트
        commentAdapter = CommentsAdapter(commentDatas, this)
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = commentAdapter

        getComment(postId.toLong(), commentAdapter)

        // 댓글 전송
        binding.ivSend.setOnClickListener {
            val commentText = binding.etComment.text.toString()
            if (commentText.isNotBlank()) {
                postComment(postId.toLong(), currentUserId, commentText, "")
            }
        }

        // 상단 메뉴 (수정/삭제)
        binding.ivPostMenu.setOnClickListener { view ->
            val popup = PopupMenu(this, view).apply {
                menuInflater.inflate(R.menu.post_menu, menu)
                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.menu_edit -> {
                            val editIntent = Intent(this@PostDetailActivity, EditPostActivity::class.java).apply {
                                putExtra("postId", postId)
                                putExtra("title", binding.tvPostTitle.text.toString())
                                putExtra("content", binding.tvPostContent.text.toString())
                                putExtra("videoUrl", videoUrl)
                            }
                            startActivity(editIntent)
                            true
                        }
                        R.id.menu_delete -> {
                            // TODO: 게시글 삭제 API 연결
                            Toast.makeText(this@PostDetailActivity, "게시글 삭제", Toast.LENGTH_SHORT).show()
                            true
                        }
                        else -> false
                    }
                }
            }
            popup.show()
        }

        // 상세 로드 + 좋아요 토글
        loadPostDetail(postId)

        binding.ivLike.setOnClickListener { toggleLike() }
    }

    // 댓글 조회
    private fun getComment(postId: Long, adapter: CommentsAdapter) {
        val token = getToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val svc = RetrofitObj.getRetrofit(this).create(CommentApiService::class.java)
        svc.getComments("Bearer $token", postId)
            .enqueue(object : Callback<CommentListResponse> {
                override fun onResponse(call: Call<CommentListResponse>, response: Response<CommentListResponse>) {
                    val resp = response.body()
                    if (!response.isSuccessful || resp == null) {
                        Log.e("GetComment", "응답 실패 code=${response.code()}")
                        return
                    }
                    if (resp.isSuccess) {
                        val comments = resp.result.orEmpty()
                        commentDatas.clear()
                        commentDatas.addAll(comments)
                        commentAdapter.notifyDataSetChanged()
                    } else {
                        Log.e("GetComment", "실패 code=${resp.code} msg=${resp.message}")
                    }
                }
                override fun onFailure(call: Call<CommentListResponse>, t: Throwable) {
                    Toast.makeText(this@PostDetailActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.d("RETROFIT/FAILURE", t.message.toString())
                }
            })
    }

    // 댓글 등록
    private fun postComment(postId: Long, userId: Long, content: String, parentId: String) {
        val token = getToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val commentApi = RetrofitObj.getRetrofit(this).create(CommentApiService::class.java)
        val request = CommentRequest(content = content, parentId = parentId)
        commentApi.createComment("Bearer $token", postId, userId, request)
            .enqueue(object : Callback<CommentResponse> {
                override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@PostDetailActivity, "댓글 등록 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        return
                    }
                    val newComment = response.body()?.result
                    if (newComment != null) {
                        commentDatas.add(newComment)
                        commentAdapter.notifyItemInserted(commentDatas.size - 1)
                        binding.rvComments.scrollToPosition(commentDatas.size - 1)
                        binding.etComment.text.clear()
                        Toast.makeText(this@PostDetailActivity, "댓글 등록 성공", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                    Toast.makeText(this@PostDetailActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.d("RETROFIT/FAILURE", t.message.toString())
                }
            })
    }

    // 댓글 삭제 (userId 경로 제거: 로그의 IllegalArgumentException 해결)
    private fun deleteComment(postId: Long, commentId: Long) {
        val token = getToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val commentApi = RetrofitObj.getRetrofit(this).create(CommentApiService::class.java)
        commentApi.deleteComment("Bearer $token", postId, commentId)
            .enqueue(object : Callback<CommonResponse> {
                override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                    val body = response.body()
                    if (response.isSuccessful && body?.isSuccess == true) {
                        val index = commentDatas.indexOfFirst { it.id == commentId.toInt() }
                        if (index != -1) {
                            commentDatas.removeAt(index)
                            commentAdapter.notifyItemRemoved(index)
                        }
                        Toast.makeText(this@PostDetailActivity, "댓글을 삭제하였습니다", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@PostDetailActivity, "삭제 실패: ${body?.message ?: response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                    Toast.makeText(this@PostDetailActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // CommentActionListener 구현 - 대댓글 등록
    override fun onReplyClicked(comment: Comment) {
        val builder = AlertDialog.Builder(this)
            .setTitle("대댓글 작성")

        val input = EditText(this).apply { hint = "대댓글 내용을 입력하세요" }
        builder.setView(input)
        builder.setPositiveButton("등록") { _, _ ->
            val replyText = input.text.toString()
            if (replyText.isNotBlank()) {
                postComment(postId.toLong(), currentUserId, replyText, comment.id.toString())

                // 로컬 업데이트
                val index = commentDatas.indexOfFirst { it.id == comment.id }
                if (index != -1) {
                    val newReply = Reply(
                        id = 0,
                        content = replyText,
                        name = "내 닉네임",
                        dogBreed = "",
                        profileImageUrl = "",
                        timestamp = System.currentTimeMillis().toString()
                    )
                    val updatedReplies = comment.replies.toMutableList().apply { add(newReply) }
                    commentDatas[index] = comment.copy(replies = updatedReplies)
                    commentAdapter.notifyItemChanged(index)
                }
            } else {
                Toast.makeText(this, "대댓글 내용을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("취소") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    // CommentActionListener 구현 - 댓글 삭제
    override fun onDeleteClicked(comment: Comment) {
        AlertDialog.Builder(this)
            .setTitle("댓글 삭제")
            .setMessage("정말 이 댓글을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                deleteComment(postId.toLong(), comment.id.toLong())
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    // 게시글 상세
    private fun loadPostDetail(postId: Int) {
        val token = getToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val boardApi = RetrofitObj.getRetrofit(this)
            .create(com.example.dogcatsquare.data.api.BoardApiService::class.java)

        boardApi.getPost("Bearer $token", postId)
            .enqueue(object : Callback<PostDetailResponse> {
                override fun onResponse(call: Call<PostDetailResponse>, response: Response<PostDetailResponse>) {
                    Log.d("PostDetailActivity", "API Response Code: ${response.code()}")
                    if (!response.isSuccessful) {
                        Toast.makeText(this@PostDetailActivity, "게시글 조회 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val postDetail = response.body()?.result
                    Log.d("PostDetailActivity", "Post detail received: $postDetail")

                    if (postDetail == null) {
                        Toast.makeText(this@PostDetailActivity, "게시글 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                        return
                    }

                    binding.tvPostTitle.text = postDetail.title ?: ""
                    binding.tvPostContent.text = postDetail.content ?: ""
                    like_count = postDetail.likeCount ?: 0
                    binding.tvLikeCount.text = like_count.toString()
                    binding.tvCommentCount.text = (postDetail.commentCount ?: 0).toString()
                    binding.tvDate.text = postDetail.createdAt ?: ""
                    binding.tvUsername.text = postDetail.username ?: ""

                    // 유튜브 썸네일
                    videoUrl = postDetail.thumbnailUrl
                    val youtubeThumb = postDetail.thumbnailUrl?.let { getYoutubeThumbnailUrl(it) }
                    if (!youtubeThumb.isNullOrEmpty()) {
                        binding.ivYoutubeThumbnail.visibility = View.VISIBLE
                        Glide.with(this@PostDetailActivity)
                            .load(youtubeThumb)
                            .placeholder(R.drawable.ic_placeholder)
                            .into(binding.ivYoutubeThumbnail)
                        binding.ivYoutubeThumbnail.setOnClickListener {
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(postDetail.thumbnailUrl)))
                        }
                    } else {
                        binding.ivYoutubeThumbnail.setImageDrawable(null)
                        binding.ivYoutubeThumbnail.visibility = View.GONE
                        binding.ivYoutubeThumbnail.setOnClickListener(null)
                    }

                    // 프로필 이미지
                    Glide.with(this@PostDetailActivity)
                        .load(postDetail.profileImageUrl)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(binding.ivProfile)

                    // 이미지 배열
                    val imageViews = listOf(
                        binding.ivPostImage1, binding.ivPostImage2, binding.ivPostImage3,
                        binding.ivPostImage4, binding.ivPostImage5
                    )
                    val images = postDetail.images.orEmpty()
                    imageViews.forEachIndexed { i, iv ->
                        if (i < images.size) {
                            iv.visibility = View.VISIBLE
                            Glide.with(this@PostDetailActivity)
                                .load(images[i])
                                .placeholder(R.drawable.ic_placeholder)
                                .error(R.drawable.ic_placeholder)
                                .into(iv)
                        } else {
                            iv.setImageDrawable(null)
                            iv.visibility = View.GONE
                        }
                    }

                    // 로컬 좋아요 상태 재적용
                    setLikeButtonState(isLiked)
                    postViewModel.updateLikeStatus(postId, isLiked)
                }

                override fun onFailure(call: Call<PostDetailResponse>, t: Throwable) {
                    Log.e("PostDetailActivity", "API 호출 실패", t)
                    Toast.makeText(this@PostDetailActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    fun getYoutubeThumbnailUrl(videoUrl: String): String? {
        val regex = "(?:youtube\\.com/(?:watch\\?v=|embed/)|youtu\\.be/)([a-zA-Z0-9_-]{6,})".toRegex()
        val id = regex.find(videoUrl)?.groupValues?.getOrNull(1) ?: return null
        return "https://img.youtube.com/vi/$id/0.jpg"
    }

    private fun toggleLike() {
        val token = getToken()
        val userId = getUserId()
        if (token.isNullOrBlank() || userId == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val retrofit = RetrofitObj.getRetrofit(this).create(PostApiService::class.java)
        retrofit.fetchLike("Bearer $token", postId, userId)
            .enqueue(object : Callback<LikeResponse> {
                override fun onResponse(call: Call<LikeResponse>, response: Response<LikeResponse>) {
                    if (!response.isSuccessful) return
                    val resultMsg = response.body()?.result.orEmpty()

                    when (resultMsg) {
                        "좋아요가 추가되었습니다." -> {
                            isLiked = true
                            like_count = (like_count + 1).coerceAtLeast(0)
                        }
                        "좋아요가 취소되었습니다." -> {
                            isLiked = false
                            like_count = (like_count - 1).coerceAtLeast(0)
                        }
                        else -> {
                            // 서버 메시지가 바뀌어도 최소한 UI는 동기화
                            isLiked = !isLiked
                            like_count = if (isLiked) (like_count + 1) else (like_count - 1).coerceAtLeast(0)
                        }
                    }

                    binding.tvLikeCount.text = like_count.toString()
                    setLikeButtonState(isLiked)
                    postViewModel.updateLikeStatus(postId, isLiked)
                    likePref.edit().putBoolean("liked_$postId", isLiked).apply()
                }

                override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                    Toast.makeText(this@PostDetailActivity, "좋아요 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setLikeButtonState(isLiked: Boolean) {
        binding.ivLike.setImageResource(if (isLiked) R.drawable.ic_liked else R.drawable.ic_like)
    }

    override fun onResume() {
        super.onResume()
        val token = getToken()
        if (!token.isNullOrBlank()) {
            loadPostDetail(postId)
            getComment(postId.toLong(), commentAdapter)
            setLikeButtonState(isLiked) // 로컬 상태 재적용
        }
    }
}