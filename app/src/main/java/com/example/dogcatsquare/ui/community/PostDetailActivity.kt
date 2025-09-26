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
import com.example.dogcatsquare.data.api.BoardApiService
import com.example.dogcatsquare.data.api.CommentApiService
import com.example.dogcatsquare.data.model.community.ApiResponse
import com.example.dogcatsquare.data.model.community.Comment
import com.example.dogcatsquare.data.model.community.CommentRequest
import com.example.dogcatsquare.data.model.community.LikeResponse
import com.example.dogcatsquare.data.model.community.PostDetail
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

    private val currentUserId: Long = 1L

    private val postViewModel: PostViewModel by viewModels()

    private var postId: Int = -1
    private var isLiked: Boolean = false
    private var like_count: Int = 0

    private val commentDatas = ArrayList<Comment>()
    private var videoUrl: String? = null

    private lateinit var likePref: SharedPreferences

    private var boardTypeFromDetail: String? = null

    private fun getToken(): String? {
        val sp = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sp.getString("token", null)
    }

    private fun getUserId(): Long? {
        val sp = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sp.getInt("userId", -1).takeIf { it != -1 }?.toLong()
    }

    private fun getUserIdAsInt(): Int? = getUserId()?.toInt()

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

        // 뒤로가기
        binding.ivBack.setOnClickListener { finish() }

        // 댓글 리스트
        commentAdapter = CommentsAdapter(commentDatas, this)
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = commentAdapter

        getComments(postId.toLong())

        // 댓글 전송
        binding.ivSend.setOnClickListener {
            val commentText = binding.etComment.text.toString()
            if (commentText.isNotBlank()) {
                // userId는 Long으로 전달
                postComment(postId.toLong(), getUserId() ?: currentUserId, commentText, "")
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
                                // ★ 추가: 상세에서 받은 게시판 타입 전달 (없으면 "자유게시판")
                                putExtra("boardType", boardTypeFromDetail ?: "자유게시판")
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

    // ===== 댓글 조회 =====
    private fun getComments(postId: Long) {
        val token = getToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val svc = RetrofitObj.getRetrofit(this).create(CommentApiService::class.java)
        svc.getComments("Bearer $token", postId)
            .enqueue(object : Callback<ApiResponse<List<Comment>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<Comment>>>,
                    response: Response<ApiResponse<List<Comment>>>
                ) {
                    if (!response.isSuccessful) {
                        Log.e("GetComment", "응답 실패 code=${response.code()}")
                        return
                    }
                    val body = response.body()
                    if (body?.isSuccess == true) {
                        val comments = body.result.orEmpty()
                        commentDatas.clear()
                        commentDatas.addAll(comments)
                        commentAdapter.notifyDataSetChanged()
                    } else {
                        Log.e("GetComment", "실패 code=${body?.code} msg=${body?.message}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse<List<Comment>>>, t: Throwable) {
                    Toast.makeText(this@PostDetailActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.d("RETROFIT/FAILURE", t.message.toString())
                }
            })
    }

    // ===== 댓글 등록 =====
    private fun postComment(postId: Long, userId: Long, content: String, parentId: String) {
        val token = getToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val commentApi = RetrofitObj.getRetrofit(this).create(CommentApiService::class.java)
        val request = CommentRequest(content = content, parentId = parentId)

        commentApi.createComment("Bearer $token", postId, userId, request)
            .enqueue(object : Callback<ApiResponse<Comment>> {
                override fun onResponse(
                    call: Call<ApiResponse<Comment>>,
                    response: Response<ApiResponse<Comment>>
                ) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@PostDetailActivity, "댓글 등록 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        return
                    }
                    val body = response.body()
                    val newComment = body?.result
                    if (body?.isSuccess == true && newComment != null) {
                        commentDatas.add(newComment)
                        commentAdapter.notifyItemInserted(commentDatas.size - 1)
                        binding.rvComments.scrollToPosition(commentDatas.size - 1)
                        binding.etComment.text.clear()
                        Toast.makeText(this@PostDetailActivity, "댓글 등록 성공", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@PostDetailActivity, body?.message ?: "댓글 등록 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<Comment>>, t: Throwable) {
                    Toast.makeText(this@PostDetailActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.d("RETROFIT/FAILURE", t.message.toString())
                }
            })
    }

    // ===== 댓글 삭제 =====
    private fun deleteComment(postId: Long, commentId: Long) {
        val token = getToken() ?: return
        val myUserId = getUserId()?.toLong() ?: return

        val api = RetrofitObj.getRetrofit(this).create(CommentApiService::class.java)
        api.deleteComment("Bearer $token", postId, commentId, myUserId)
            .enqueue(object : Callback<ApiResponse<Unit>> {
                override fun onResponse(
                    call: Call<ApiResponse<Unit>>,
                    response: Response<ApiResponse<Unit>>
                ) {
                    val body = response.body()
                    if (response.isSuccessful && body?.isSuccess == true) {
                        val idx = commentDatas.indexOfFirst { it.id == commentId.toInt() }
                        if (idx != -1) {
                            commentDatas.removeAt(idx)
                            commentAdapter.notifyItemRemoved(idx)
                        }
                        Toast.makeText(this@PostDetailActivity, "댓글을 삭제했어요.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            this@PostDetailActivity,
                            body?.message ?: "삭제 실패 (${response.code()})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<Unit>>, t: Throwable) {
                    Toast.makeText(this@PostDetailActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // ===== CommentActionListener 구현 - 대댓글 등록 =====
    override fun onReplyClicked(comment: Comment) {
        val input = EditText(this).apply { hint = "대댓글 내용을 입력하세요" }
        AlertDialog.Builder(this)
            .setTitle("대댓글 작성")
            .setView(input)
            .setPositiveButton("등록") { _, _ ->
                val replyText = input.text.toString()
                if (replyText.isNotBlank()) {
                    postComment(postId.toLong(), getUserId() ?: currentUserId, replyText, comment.id.toString())

                    // 로컬 업데이트(서버 반영 전 임시 표시)
                    val index = commentDatas.indexOfFirst { it.id == comment.id }
                    if (index != -1) {
                        val newReply = com.example.dogcatsquare.data.model.community.Reply(
                            id = 0,
                            content = replyText,
                            name = "내 닉네임",
                            dogBreed = "",
                            profileImageUrl = "",
                            timestamp = System.currentTimeMillis().toString()
                        )
                        val updated = comment.replies.toMutableList().apply { add(newReply) }
                        commentDatas[index] = comment.copy(replies = updated)
                        commentAdapter.notifyItemChanged(index)
                    }
                } else {
                    Toast.makeText(this, "대댓글 내용을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소") { d, _ -> d.cancel() }
            .show()
    }

    // ===== CommentActionListener 구현 - 댓글 삭제 =====
    override fun onDeleteClicked(comment: Comment) {
        AlertDialog.Builder(this)
            .setTitle("댓글 삭제")
            .setMessage("정말 이 댓글을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                deleteComment(postId.toLong(), comment.id.toLong())
            }
            .setNegativeButton("취소") { d, _ -> d.cancel() }
            .show()
    }

    // ===== 게시글 상세 =====
    private fun loadPostDetail(postId: Int) {
        val token = getToken()
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val boardApi = RetrofitObj.getRetrofit(this).create(BoardApiService::class.java)
        boardApi.getPost("Bearer $token", postId)
            .enqueue(object : Callback<ApiResponse<PostDetail>> {
                override fun onResponse(
                    call: Call<ApiResponse<PostDetail>>,
                    response: Response<ApiResponse<PostDetail>>
                ) {
                    Log.d("PostDetailActivity", "API Response Code: ${response.code()}")
                    if (!response.isSuccessful) {
                        Toast.makeText(this@PostDetailActivity, "게시글 조회 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val body = response.body()
                    val postDetail = body?.result
                    if (body?.isSuccess != true || postDetail == null) {
                        Toast.makeText(this@PostDetailActivity, body?.message ?: "게시글 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                        return
                    }

                    boardTypeFromDetail = postDetail.boardType

                    binding.tvPostTitle.text = postDetail.title ?: ""
                    binding.tvPostContent.text = postDetail.content ?: ""
                    like_count = postDetail.likeCount ?: 0
                    binding.tvLikeCount.text = like_count.toString()
                    binding.tvCommentCount.text = (postDetail.commentCount ?: 0).toString()
                    binding.tvDate.text = postDetail.createdAt ?: ""
                    binding.tvUsername.text = postDetail.username ?: ""

                    // 유튜브 썸네일
                    videoUrl = postDetail.videoUrl
                    val youtubeThumb = postDetail.videoUrl?.let { getYoutubeThumbnailUrl(it) }
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

                    Glide.with(this@PostDetailActivity)
                        .load(postDetail.profileImageUrl)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(binding.ivProfile)

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

                    setLikeButtonState(isLiked)
                    postViewModel.updateLikeStatus(postId, isLiked)
                }

                override fun onFailure(call: Call<ApiResponse<PostDetail>>, t: Throwable) {
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
        val userIdInt = getUserIdAsInt()
        if (token.isNullOrBlank() || userIdInt == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val retrofit = RetrofitObj.getRetrofit(this).create(PostApiService::class.java)
        retrofit.fetchLike("Bearer $token", postId, userIdInt)
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
            getComments(postId.toLong())
            setLikeButtonState(isLiked)
        }
    }
}