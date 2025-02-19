package com.example.dogcatsquare.ui.community

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.api.CommentApiService
import com.example.dogcatsquare.data.community.Comment
import com.example.dogcatsquare.data.community.CommentRequest
import com.example.dogcatsquare.data.community.CommentResponse
import com.example.dogcatsquare.data.community.CommonResponse
import com.example.dogcatsquare.data.community.PostDetailResponse
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.ActivityPostDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostDetailActivity : AppCompatActivity(), CommentActionListener {

    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var commentAdapter: CommentsAdapter
    private lateinit var comments: MutableList<Comment>

    // 클래스 멤버 변수로 선언하여 모든 메서드에서 접근 가능하게 함
    private var postId: Long = 0L
    private val currentUserId: Long = 1L  // 실제 앱에서는 로그인한 사용자 ID 사용

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // postId를 클래스 멤버에 할당 (예: getIntExtra를 사용한 후 toLong() 변환)
        postId = intent.getIntExtra("postId", -1).toLong()
        if (postId == -1L) {
            Toast.makeText(this, "게시글 ID가 전달되지 않았습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 뒤로가기 버튼
        binding.ivBack.setOnClickListener { finish() }

        // 댓글 RecyclerView 설정
        // 임시 데이터: 실제 API 응답 시 id가 올바르게 채워져 있어야 함
        comments = mutableListOf(
            Comment(
                id = 1,
                content = "더 열심히 놀아주세요!",
                name = "닉네임1",
                animalType = "",
                profileImageUrl = "",
                timestamp = "2021.01.01",
                replies = listOf("첫 번째 대댓글", "두 번째 대댓글")
            ),
            Comment(
                id = 2,
                content = "대댓",
                name = "닉네임2",
                animalType = "",
                profileImageUrl = "",
                timestamp = "2021.01.01",
                replies = emptyList()
            )
        )

        commentAdapter = CommentsAdapter(comments, this)
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = commentAdapter

        // 댓글 입력 후 전송 버튼 클릭 시 처리
        binding.ivSend.setOnClickListener {
            val commentText = binding.etComment.text.toString()
            if (commentText.isNotBlank()) {
                // 일반 댓글의 경우 parentId는 빈 문자열("")
                postComment(postId, currentUserId, commentText, "")
            }
        }

        // 게시글 상세 정보 로드
        loadPostDetail(postId.toInt())
    }

    // 댓글 등록 API 호출 함수 (클래스 멤버 함수)
    private fun postComment(postId: Long, userId: Long, content: String, parentId: String) {
        val commentApi = RetrofitObj.getRetrofit().create(CommentApiService::class.java)
        val request = CommentRequest(content = content, parentId = parentId)
        commentApi.createComment(postId, userId, request).enqueue(object : Callback<CommentResponse> {
            override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                if (response.isSuccessful) {
                    val newComment = response.body()?.result
                    if (newComment != null) {
                        comments.add(newComment)
                        commentAdapter.notifyItemInserted(comments.size - 1)
                        binding.rvComments.scrollToPosition(comments.size - 1)
                        binding.etComment.text.clear()
                        Toast.makeText(this@PostDetailActivity, "댓글 등록 성공", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@PostDetailActivity, "댓글 등록 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                Toast.makeText(this@PostDetailActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 댓글 삭제 API 호출 함수 (클래스 멤버 함수)
    private fun deleteComment(postId: Long, commentId: Long) {
        val commentApi = RetrofitObj.getRetrofit().create(CommentApiService::class.java)
        commentApi.deleteComment(postId, commentId).enqueue(object : Callback<CommonResponse> {
            override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val index = comments.indexOfFirst { it.id == commentId.toInt() }
                    if (index != -1) {
                        comments.removeAt(index)
                        commentAdapter.notifyItemRemoved(index)
                    }
                    Toast.makeText(this@PostDetailActivity, "댓글 삭제 성공", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@PostDetailActivity, "댓글 삭제 실패", Toast.LENGTH_SHORT).show()
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
        builder.setTitle("대댓글 작성")
        val input = EditText(this)
        input.hint = "대댓글 내용을 입력하세요"
        builder.setView(input)
        builder.setPositiveButton("등록") { dialog, which ->
            val replyText = input.text.toString()
            if (replyText.isNotBlank()) {
                // API 호출: 서버에서 대댓글을 부모 댓글의 replies 필드에 추가하는 로직이 필요
                postComment(postId, currentUserId, replyText, comment.id.toString())

                // 로컬 업데이트: 기존 댓글의 replies 필드만 업데이트
                val index = comments.indexOfFirst { it.id == comment.id }
                if (index != -1) {
                    val updatedReplies = comment.replies.toMutableList().apply { add(replyText) }
                    // 부모 댓글 객체를 복사하여 replies 필드만 업데이트
                    comments[index] = comment.copy(replies = updatedReplies)
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
    }

    // CommentActionListener 구현 - 댓글 삭제
    override fun onDeleteClicked(comment: Comment) {
        AlertDialog.Builder(this)
            .setTitle("댓글 삭제")
            .setMessage("정말 이 댓글을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { dialog, which ->
                deleteComment(postId, comment.id.toLong())
            }
            .setNegativeButton("취소") { dialog, which ->
                dialog.cancel()
            }
            .show()
    }

    // 게시글 상세 정보 로드 함수 (기존 코드)
    private fun loadPostDetail(postId: Int) {
        val boardApiService = RetrofitObj.getRetrofit().create(
            com.example.dogcatsquare.data.api.BoardApiService::class.java
        )
        boardApiService.getPost(postId).enqueue(object : Callback<PostDetailResponse> {
            override fun onResponse(call: Call<PostDetailResponse>, response: Response<PostDetailResponse>) {
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
}
