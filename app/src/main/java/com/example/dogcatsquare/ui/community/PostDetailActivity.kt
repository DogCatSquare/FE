package com.example.dogcatsquare.ui.community

import PostApiService
import android.content.Context
import android.content.Intent
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
import com.example.dogcatsquare.data.community.Comment
import com.example.dogcatsquare.data.community.CommentListResponse
import com.example.dogcatsquare.data.community.CommentRequest
import com.example.dogcatsquare.data.community.CommentResponse
import com.example.dogcatsquare.data.community.CommonResponse
import com.example.dogcatsquare.data.community.LikeResponse
import com.example.dogcatsquare.data.community.PostDetailResponse
import com.example.dogcatsquare.data.community.Reply
import com.example.dogcatsquare.data.model.home.Event
import com.example.dogcatsquare.data.model.home.GetAllEventsResponse
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.ActivityPostDetailBinding
import com.example.dogcatsquare.ui.viewmodel.PostViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostDetailActivity : AppCompatActivity(), CommentActionListener {

    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var commentAdapter: CommentsAdapter

    // 클래스 멤버 변수로 선언하여 모든 메서드에서 접근 가능하게 함
    private val currentUserId: Long = 1L  // 실제 앱에서는 로그인한 사용자 ID 사용
    private val postViewModel: PostViewModel by viewModels()
    private var postId: Int = -1
    private var isLiked: Boolean = false  // 현재 좋아요 상태 저장
    private var likeCount: Int = 0  // 좋아요 개수 저장

    private var commentDatas = ArrayList<Comment>()

    private fun getToken(): String? {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

    private fun getUserId(): Int? {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getInt("userId", -1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // postId를 클래스 멤버에 할당 (예: getIntExtra를 사용한 후 toLong() 변환)
        postId = intent.getIntExtra("postId", -1)
        if (postId == -1) {
            Toast.makeText(this, "게시글 ID가 전달되지 않았습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        postId = intent.getIntExtra("postId", -1)

        // 뒤로가기 버튼
        binding.ivBack.setOnClickListener { finish() }

        commentAdapter = CommentsAdapter(commentDatas, this)
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = commentAdapter
        getComment(postId.toLong(), commentAdapter)

        // 댓글 입력 후 전송 버튼 클릭 시 처리
        binding.ivSend.setOnClickListener {
            val commentText = binding.etComment.text.toString()
            if (commentText.isNotBlank()) {
                // 일반 댓글의 경우 parentId는 빈 문자열("")
                postComment(postId.toLong(), currentUserId, commentText, "")
            }
        }

        // onCreate()에서 뷰 초기화 후
        binding.ivPostMenu.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.post_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_edit -> {
                        // 수정 화면으로 전환 (필요한 데이터 추가 전달)
                        val editIntent = Intent(this, EditPostActivity::class.java).apply {
                            putExtra("postId", postId)
                            putExtra("title", binding.tvPostTitle.text.toString())
                            putExtra("content", binding.tvPostContent.text.toString())
                            // 예: putExtra("videoUrl", ...), putExtra("imageUrl", ...) 등
                        }
                        startActivity(editIntent)
                        true
                    }
                    R.id.menu_delete -> {
                        // 삭제 로직 구현 (예: API 호출 후 결과 처리)
                        Toast.makeText(this, "게시글 삭제", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        // 게시글 상세 정보 로드
        loadPostDetail(postId.toInt())


    }

    // 댓글 조회
    private fun getComment(postId: Long, adapter: CommentsAdapter) {
        val token = getToken()

        val getCommentService = RetrofitObj.getRetrofit().create(CommentApiService::class.java)
        getCommentService.getComments("Bearer $token", postId).enqueue(object : Callback<CommentListResponse> {
            override fun onResponse(call: Call<CommentListResponse>, response: Response<CommentListResponse>) {
                response.body()?.let { resp ->
                    if (resp.isSuccess) {
                        Log.d("GetComment", "댓글 전체 조회 성공")

                        val comments = resp.result
                        if (comments != null) {
                            commentDatas.clear() // 기존 데이터 삭제 후 추가
                            commentDatas.addAll(comments)

                            Log.d("CommentList", commentDatas.toString())
                            adapter.notifyDataSetChanged() // UI 갱신
                        } else {
                            Log.d("GetComment", "댓글이 없음")
                        }
                    } else {
                        Log.e("GetComment/FAILURE", "응답 실패: ${resp.code}, 메시지: ${resp.message}")
                    }
                } ?: run {
                    Log.e("GetComment/ERROR", "서버 응답이 null임")
                }
            }
            override fun onFailure(call: Call<CommentListResponse>, t: Throwable) {
                Toast.makeText(this@PostDetailActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }
        })
    }

    // 댓글 등록 API 호출 함수 (클래스 멤버 함수)
    private fun postComment(postId: Long, userId: Long, content: String, parentId: String) {
        val token = getToken()

        val commentApi = RetrofitObj.getRetrofit().create(CommentApiService::class.java)
        val request = CommentRequest(content = content, parentId = parentId)
        commentApi.createComment("Bearer $token", postId, userId, request).enqueue(object : Callback<CommentResponse> {
            override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                if (response.isSuccessful) {
                    val newComment = response.body()?.result
                    if (newComment != null) {
                        commentDatas.add(newComment)
                        commentAdapter.notifyItemInserted(commentDatas.size - 1)
                        binding.rvComments.scrollToPosition(commentDatas.size - 1)
                        binding.etComment.text.clear()
                        Toast.makeText(this@PostDetailActivity, "댓글 등록 성공", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@PostDetailActivity, "댓글 등록 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                Toast.makeText(this@PostDetailActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }
        })
    }

    // 댓글 삭제 API 호출 함수 (클래스 멤버 함수)
    private fun deleteComment(postId: Long, commentId: Long) {
        val token = getToken()
        val userId = getUserId()

        val commentApi = RetrofitObj.getRetrofit().create(CommentApiService::class.java)
        if (userId != null) {
            commentApi.deleteComment("Bearer $token", postId, commentId, userId.toInt()).enqueue(object : Callback<CommonResponse> {
                override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        val index = commentDatas.indexOfFirst { it.id == commentId.toInt() }
                        if (index != -1) {
                            commentDatas.removeAt(index)
                            commentAdapter.notifyItemRemoved(index)
                        }
                        Toast.makeText(this@PostDetailActivity, "댓글을 삭제하였습니다", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@PostDetailActivity, "내가 작성한 댓글이 아닙니다", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                    Toast.makeText(this@PostDetailActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // CommentActionListener 구현 - 대댓글 등록
    override fun onReplyClicked(comment: Comment) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("대댓글 작성")
        val input = EditText(this)
        input.hint = "대댓글 내용을 입력하세요"
        builder.setView(input)
        builder.setPositiveButton("등록") { dialog, which ->
            val replyText = input.text.toString()
            if (replyText.isNotBlank()) {
                // API 호출: 서버에서 대댓글을 부모 댓글의 replies 필드에 추가하는 로직이 필요
                postComment(postId.toLong(), currentUserId, replyText, comment.id.toString())

                // 로컬 업데이트: 기존 댓글의 replies 필드만 업데이트
                val index = commentDatas.indexOfFirst { it.id == comment.id }
                if (index != -1) {
                    val newReply = Reply(
                        id = 0, // 실제 id는 서버에서 받아오거나 적절한 값을 할당하세요.
                        content = replyText,
                        name = "내 닉네임", // 현재 사용자 닉네임을 넣으세요.
                        dogBreed = "", // 필요 시 적절한 값을 넣으세요.
                        profileImageUrl = "", // 사용자 프로필 이미지 URL
                        timestamp = System.currentTimeMillis().toString() // 또는 원하는 포맷의 시간
                    )

                    val updatedReplies = comment.replies.toMutableList().apply { add(newReply) }
                    // 부모 댓글 객체를 복사하여 replies 필드만 업데이트
                    commentDatas[index] = comment.copy(replies = updatedReplies)
                    commentAdapter.notifyItemChanged(index)
                }
            } else {
                Toast.makeText(this, "대댓글 내용을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("취소") { dialog, which ->
            dialog.cancel()
        }
        builder.show()

        binding.ivLike.setOnClickListener {
            toggleLike()
        }

        postViewModel.likedPosts.observe(this) { likedPosts ->
            likedPosts[postId]?.let { isLiked ->
                setLikeButtonState(isLiked)
            }
        }
    }

    // CommentActionListener 구현 - 댓글 삭제
    override fun onDeleteClicked(comment: Comment) {
        AlertDialog.Builder(this)
            .setTitle("댓글 삭제")
            .setMessage("정말 이 댓글을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { dialog, which ->
                deleteComment(postId.toLong(), comment.id.toLong())
            }
            .setNegativeButton("취소") { dialog, which ->
                dialog.cancel()
            }
            .show()
    }

    // 게시글 상세 정보 로드 함수 (기존 코드)
    private fun loadPostDetail(postId: Int) {
        val token = getToken()
        val boardApiService = RetrofitObj.getRetrofit().create(
            com.example.dogcatsquare.data.api.BoardApiService::class.java
        )
        boardApiService.getPost("Bearer $token", postId).enqueue(object : Callback<PostDetailResponse> {
            override fun onResponse(
                call: Call<PostDetailResponse>,
                response: Response<PostDetailResponse>
            ) {
                Log.d("PostDetailActivity", "API Response Code: ${response.code()}")
                if (response.isSuccessful) {
                    val postDetail = response.body()?.result
                    Log.d("PostDetailActivity", "Post detail received: $postDetail")
                    if (postDetail != null) {
                        binding.tvPostTitle.text = postDetail.title
                        binding.tvPostContent.text = postDetail.content
                        binding.tvLikeCount.text = postDetail.likeCount.toString()
                        binding.tvCommentCount.text = postDetail.commentCount.toString()
                        binding.tvDate.text = postDetail.createdAt
                        Glide.with(this@PostDetailActivity)
                            .load(postDetail.profileImageUrl)
                            .placeholder(R.drawable.ic_profile_placeholder)
                            .into(binding.ivProfile)

                        // 이미지 처리
                        if (!postDetail.images.isNullOrEmpty()) {
                            val imageViews = listOf(
                                binding.ivPostImage1,
                                binding.ivPostImage2,
                                binding.ivPostImage3,
                                binding.ivPostImage4,
                                binding.ivPostImage5
                            )
                            for (i in imageViews.indices) {
                                if (i < postDetail.images.size) {
                                    imageViews[i].visibility = View.VISIBLE
                                    Glide.with(this@PostDetailActivity)
                                        .load(postDetail.images[i])
                                        .placeholder(R.drawable.ic_placeholder)
                                        .into(imageViews[i])
                                } else {
                                    imageViews[i].visibility = View.GONE
                                }
                            }
                        } else {
                            binding.ivPostImage1.visibility = View.GONE
                            binding.ivPostImage2.visibility = View.GONE
                            binding.ivPostImage3.visibility = View.GONE
                            binding.ivPostImage4.visibility = View.GONE
                            binding.ivPostImage5.visibility = View.GONE
                        }

                        postViewModel.updateLikeStatus(postId, postDetail.likeCount > 0)
                    } else {
                        Toast.makeText(this@PostDetailActivity, "게시글 정보가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@PostDetailActivity, "게시글 조회 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<PostDetailResponse>, t: Throwable) {
                Log.e("PostDetailActivity", "API 호출 실패", t)
                Toast.makeText(this@PostDetailActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun toggleLike() {
        val retrofit = RetrofitObj.getRetrofit().create(PostApiService::class.java)
        val token = getToken()
        val userId = getUserId()  // 실제 유저 ID로 변경

        if (userId != null) {
            retrofit.fetchLike("Bearer $token", postId, userId).enqueue(object : Callback<LikeResponse> {
                override fun onResponse(call: Call<LikeResponse>, response: Response<LikeResponse>) {
                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                        isLiked = response.body()?.result == "좋아요가 추가되었습니다."
                        likeCount = if (isLiked) likeCount + 1 else likeCount
                        binding.tvLikeCount.text = likeCount.toString()

                        // ✅ ViewModel에 좋아요 상태 업데이트
                        postViewModel.updateLikeStatus(postId, isLiked)
                    }
                }

                override fun onFailure(call: Call<LikeResponse>, t: Throwable) {
                    Toast.makeText(this@PostDetailActivity, "좋아요 실패", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun setLikeButtonState(isLiked: Boolean) {
        binding.ivLike.setImageResource(if (isLiked) R.drawable.ic_liked else R.drawable.ic_like)
    }

    override fun onResume() {
        super.onResume()
        val token = getToken()
        if (token != null) {
            loadPostDetail(postId)
            getComment(postId.toLong(), commentAdapter)
        }
    }
}