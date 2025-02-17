package com.example.dogcatsquare.ui.community

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.community.PostDetailResponse
import com.example.dogcatsquare.data.network.RetrofitObj
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostDetailActivity : AppCompatActivity() {

    private lateinit var commentAdapter: CommentAdapter
    private lateinit var comments: MutableList<Comment>

    // 게시글 정보를 표시할 TextView
    private lateinit var tvPostTitle: TextView
    private lateinit var tvPostContent: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        val postId: Int = intent.getIntExtra("postId", -1)

        // 뷰 초기화
        val ivBack = findViewById<ImageView>(R.id.ivBack)
        val rvComments = findViewById<RecyclerView>(R.id.rvComments)
        val etComment = findViewById<EditText>(R.id.etComment)
        val ivSend = findViewById<ImageView>(R.id.ivSend)
        tvPostTitle = findViewById(R.id.tvPostTitle)
        tvPostContent = findViewById(R.id.tvPostContent)

        // 뒤로가기 버튼
        ivBack.setOnClickListener { finish() }

        // 댓글 기능 (더미 데이터 사용)
        comments = mutableListOf(
            Comment("닉네임1", "더 열심히 놀아주세요!", "2021.01.01"),
            Comment("닉네임2", "대댓", "2021.01.01")
        )
        commentAdapter = CommentAdapter(comments)
        rvComments.layoutManager = LinearLayoutManager(this)
        rvComments.adapter = commentAdapter

        ivSend.setOnClickListener {
            val commentText = etComment.text.toString()
            if (commentText.isNotBlank()) {
                comments.add(Comment("나", commentText, "방금 전"))
                commentAdapter.notifyItemInserted(comments.size - 1)
                rvComments.scrollToPosition(comments.size - 1)
                etComment.text.clear()
            }
        }

        // 전달받은 postId 확인
        Log.d("PostDetailActivity", "Received postId: $postId")
        if (postId != -1) {
            loadPostDetail(postId)
        } else {
            Toast.makeText(this, "게시글 ID가 전달되지 않았습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadPostDetail(postId: Int) {
        val boardApiService = RetrofitObj.getRetrofit().create(
            com.example.dogcatsquare.data.api.BoardApiService::class.java
        )
        boardApiService.getPost(postId).enqueue(object : Callback<PostDetailResponse> {
            override fun onResponse(
                call: Call<PostDetailResponse>,
                response: Response<PostDetailResponse>
            ) {
                Log.d("PostDetailActivity", "API Response Code: ${response.code()}")
                if (response.isSuccessful) {
                    val postDetail = response.body()?.result
                    Log.d("PostDetailActivity", "Post detail received: $postDetail")
                    if (postDetail != null) {
                        // API로부터 받은 게시글 정보 적용
                        tvPostTitle.text = postDetail.title
                        tvPostContent.text = postDetail.content
                        // 추가 데이터(작성자, 썸네일 등)가 필요하다면 여기에 추가 처리
                    } else {
                        Toast.makeText(
                            this@PostDetailActivity,
                            "게시글 정보가 없습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@PostDetailActivity,
                        "게시글 조회 실패: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<PostDetailResponse>, t: Throwable) {
                Log.e("PostDetailActivity", "API 호출 실패", t)
                Toast.makeText(
                    this@PostDetailActivity,
                    "네트워크 오류: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
