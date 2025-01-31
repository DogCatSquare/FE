package com.example.dogcatsquare.data.community

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dogcatsquare.R
import com.example.dogcatsquare.api.RetrofitClient
import com.example.dogcatsquare.data.community.ApiResponse
import com.example.dogcatsquare.data.community.PostRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditPostActivity : AppCompatActivity() {

    private lateinit var btnSave: ImageView
    private lateinit var etTitle: EditText
    private lateinit var etContent: EditText
    private lateinit var etLink: EditText
    private lateinit var ivBack: ImageView

    private var postId: Long = 0
    private var boardId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        btnSave = findViewById(R.id.btnComplete)
        etTitle = findViewById(R.id.etTitle)
        etContent = findViewById(R.id.etContent)
        etLink = findViewById(R.id.edit_link)
        ivBack = findViewById(R.id.iv_back)

        // 이전 화면에서 전달받은 게시글 ID 가져오기
        postId = intent.getLongExtra("POST_ID", 0)
        boardId = intent.getLongExtra("BOARD_ID", 0)

        // 이전 게시글 정보 불러오기
        etTitle.setText(intent.getStringExtra("TITLE"))
        etContent.setText(intent.getStringExtra("CONTENT"))
        etLink.setText(intent.getStringExtra("VIDEO_URL"))

        // 뒤로가기 버튼
        ivBack.setOnClickListener {
            finish()
        }

        // 저장 버튼 클릭 시 게시글 수정 API 호출
        btnSave.setOnClickListener {
            updatePost()
        }
    }

    private fun updatePost() {
        val token = "Bearer YOUR_JWT_TOKEN" // JWT 토큰 설정

        val title = etTitle.text.toString().trim()
        val content = etContent.text.toString().trim()
        val videoUrl = etLink.text.toString().trim()

        if (title.length < 2 || title.length > 15) {
            Toast.makeText(this, "제목은 2~15자여야 합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (content.length > 300) {
            Toast.makeText(this, "내용은 300자를 초과할 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val postRequest = PostRequest(
            boardId = boardId.toInt(),
            title = title,
            content = content,
            video_URL = if (videoUrl.isNotBlank()) videoUrl else "",
            created_at = "2025-01-30T15:46:13.718Z"
        )

        val call = RetrofitClient.instance.updatePost(postId, token, postRequest)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EditPostActivity, "게시글이 수정되었습니다!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditPostActivity, "게시글 수정 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@EditPostActivity, "네트워크 오류: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
