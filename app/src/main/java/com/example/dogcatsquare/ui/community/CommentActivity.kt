package com.example.dogcatsquare.ui.community

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.data.api.CommentApiService
import com.example.dogcatsquare.data.community.Comment
import com.example.dogcatsquare.data.community.CommentRequest
import com.example.dogcatsquare.data.community.CommentResponse
import com.example.dogcatsquare.data.community.CommentListResponse
import com.example.dogcatsquare.data.community.CommonResponse
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.ActivityCommentBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommentActivity : AppCompatActivity(), CommentActionListener {

    private lateinit var binding: ActivityCommentBinding
    private lateinit var commentAdapter: CommentsAdapter
    private var comments: MutableList<Comment> = mutableListOf()
    private var postId: Long = 0

    // 현재 로그인한 사용자 ID (실제 앱에서는 로그인 정보를 이용)
    private val currentUserId: Long = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postId = intent.getLongExtra("postId", -1)
        if (postId == -1L) {
            Toast.makeText(this, "게시글 ID가 전달되지 않았습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // RecyclerView 설정
        commentAdapter = CommentsAdapter(comments, this)
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = commentAdapter

        // 댓글 등록 버튼 클릭 시 처리 (일반 댓글 등록)
        binding.ivSend.setOnClickListener {
            val commentText = binding.etComment.text.toString()
            if (commentText.isNotBlank()) {
                postComment(postId, currentUserId, commentText, "")
            }
        }

        // API로 댓글 목록 로드 (상위 댓글만 표시하도록 필터링)
        loadComments(postId)
    }

    // 댓글 데이터를 변환하여 대댓글을 부모 댓글 아래에만 추가하도록 처리
    private fun processComments(response: List<Comment>): List<Comment> {
        val commentMap = mutableMapOf<Long, Comment>() // 댓글 ID를 키로 하는 맵 생성
        val parentComments = mutableListOf<Comment>() // 부모 댓글만 담는 리스트

        for (comment in response) {
            commentMap[comment.id.toLong()] = comment // ✅ Int → Long 변환
        }

        for (comment in response) {
            if (comment.parentId == null) {
                parentComments.add(comment) // ✅ 부모 댓글이면 리스트에 추가
            } else {
                // ✅ 대댓글이면 부모 댓글의 replies 리스트에 추가
                val parent = commentMap[comment.parentId.toLong()]
                if (parent != null) {
                    val updatedReplies = parent.replies.toMutableList()
                    updatedReplies.add(comment.content)
                    commentMap[comment.parentId.toLong()] = parent.copy(replies = updatedReplies) // ✅ copy() 사용
                }
            }
        }

        return parentComments
    }


    // API에서 댓글 데이터를 가져오고 대댓글을 처리하여 comments 리스트에 저장
    private fun loadComments(postId: Long) {
        val commentApi = RetrofitObj.getRetrofit().create(CommentApiService::class.java)
        commentApi.getComments(postId).enqueue(object : Callback<CommentListResponse> {
            override fun onResponse(
                call: Call<CommentListResponse>,
                response: Response<CommentListResponse>
            ) {
                if (response.isSuccessful) {
                    val processedComments = processComments(response.body()?.result ?: emptyList())
                    comments.clear()
                    comments.addAll(processedComments)
                    commentAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(
                        this@CommentActivity,
                        "댓글 로드 실패: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<CommentListResponse>, t: Throwable) {
                Toast.makeText(
                    this@CommentActivity,
                    "네트워크 오류: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // 댓글 등록 API 호출 함수
    private fun postComment(postId: Long, userId: Long, content: String, parentId: String) {
        val commentApi = RetrofitObj.getRetrofit().create(CommentApiService::class.java)
        val request = CommentRequest(content = content, parentId = parentId)

        commentApi.createComment(postId, userId, request).enqueue(object : Callback<CommentResponse> {
            override fun onResponse(call: Call<CommentResponse>, response: Response<CommentResponse>) {
                if (response.isSuccessful) {
                    val newComment = response.body()?.result
                    if (newComment != null) {
                        if (parentId.isEmpty()) {
                            // 일반 댓글 추가
                            comments.add(newComment)
                            commentAdapter.notifyItemInserted(comments.size - 1)
                        } else {
                            // 대댓글 추가 시, `comments` 리스트에 추가하지 않음
                            val parentIndex = comments.indexOfFirst { it.id.toString() == parentId }
                            if (parentIndex != -1) {
                                val updatedReplies = comments[parentIndex].replies.toMutableList()
                                updatedReplies.add(newComment.content) // 대댓글 내용 추가
                                comments[parentIndex] = comments[parentIndex].copy(replies = updatedReplies)

                                // `loadComments(postId)`를 호출하지 않고 UI만 갱신
                                commentAdapter.notifyItemChanged(parentIndex)
                            }
                        }
                        binding.etComment.text.clear()
                    }
                }
            }
            override fun onFailure(call: Call<CommentResponse>, t: Throwable) {
                Toast.makeText(this@CommentActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 댓글 삭제 API 호출 함수
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
                    Toast.makeText(this@CommentActivity, "댓글 삭제 성공", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@CommentActivity, "댓글 삭제 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                Toast.makeText(this@CommentActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // CommentActionListener 구현 - 대댓글 등록
    override fun onReplyClicked(comment: Comment) {
        // 대댓글 등록 UI 표시
        val builder = AlertDialog.Builder(this)
        builder.setTitle("대댓글 작성")
        val input = EditText(this)
        input.hint = "대댓글 내용을 입력하세요"
        builder.setView(input)
        builder.setPositiveButton("등록") { dialog, which ->
            val replyText = input.text.toString()
            if (replyText.isNotBlank()) {
                // API 호출 및 대댓글 등록 (부모 댓글의 id를 parentId로 전달)
                postComment(postId, currentUserId, replyText, comment.id.toString())
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
}
