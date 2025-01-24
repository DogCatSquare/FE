package com.example.dogcatsquare.ui.community

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R

class PostDetailActivity : AppCompatActivity() {

    private lateinit var commentAdapter: CommentAdapter
    private lateinit var comments: MutableList<Comment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        val ivBack = findViewById<ImageView>(R.id.ivBack)
        val rvComments = findViewById<RecyclerView>(R.id.rvComments)
        val etComment = findViewById<EditText>(R.id.etComment)
        val ivSend = findViewById<ImageView>(R.id.ivSend)

        // 게시글 정보 표시용 TextView 예시
        val tvPostTitle = findViewById<TextView>(R.id.tvPostTitle)
        val tvPostContent = findViewById<TextView>(R.id.tvPostContent)

        // 1) Adapter에서 넘겨준 데이터 받기
        val username = intent.getStringExtra("username")
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")

        // 2) 화면에 표시
        tvPostTitle.text = title ?: "제목 없음"
        tvPostContent.text = content ?: "내용 없음"

        // 뒤로가기 버튼 처리
        ivBack.setOnClickListener { finish() }

        // 더미 댓글 데이터
        comments = mutableListOf(
            Comment("닉네임1", "더 열심히 놀아주세요!", "2021.01.01"),
            Comment("닉네임2", "대댓", "2021.01.01")
        )

        // RecyclerView 설정
        commentAdapter = CommentAdapter(comments)
        rvComments.layoutManager = LinearLayoutManager(this)
        rvComments.adapter = commentAdapter

        // 댓글 입력 처리
        ivSend.setOnClickListener {
            val commentText = etComment.text.toString()
            if (commentText.isNotBlank()) {
                comments.add(Comment("나", commentText, "방금 전"))
                commentAdapter.notifyItemInserted(comments.size - 1)
                rvComments.scrollToPosition(comments.size - 1)
                etComment.text.clear()
            }
        }
    }
}
