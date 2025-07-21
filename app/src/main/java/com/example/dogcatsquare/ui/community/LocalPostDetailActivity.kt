package com.example.dogcatsquare.ui.community

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.dogcatsquare.R

class LocalPostDetailActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvContent: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvDogBreed: TextView
    private lateinit var ivProfile: ImageView
    private lateinit var ivPostImage1: ImageView
    private lateinit var ivPostImage2: ImageView
    private lateinit var tvDate: TextView
    private lateinit var tvLikeCount: TextView
    private lateinit var tvCommentCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // item_local_post 레이아웃을 상세 화면으로 사용
        setContentView(R.layout.item_local_post)

        // 뷰 초기화
        tvUsername = findViewById(R.id.tvUsername)
//        tvDogBreed = findViewById(R.id.tvDogBreed)
        tvTitle = findViewById(R.id.tvTitle)
        tvContent = findViewById(R.id.tvContent)
        ivProfile = findViewById(R.id.ivProfile)
        ivPostImage1 = findViewById(R.id.ivPostImage1)
        ivPostImage2 = findViewById(R.id.ivPostImage2)
        tvDate = findViewById(R.id.tvDate)
        tvLikeCount = findViewById(R.id.tvLikeCount)
        tvCommentCount = findViewById(R.id.tvCommentCount)

        // Intent로 전달된 데이터 받기
        val postId = intent.getLongExtra("postId", -1L)
        val title = intent.getStringExtra("title") ?: "제목 없음"
        val content = intent.getStringExtra("content") ?: "내용 없음"
        val username = intent.getStringExtra("username") ?: "닉네임 없음"
        val dogbreed = intent.getStringExtra("dogbreed") ?: "견종 없음"
        val date = intent.getStringExtra("date") ?: "날짜 없음"
        val likeCount = intent.getIntExtra("likeCount", 0)
        val commentCount = intent.getIntExtra("commentCount", 0)
        val imageResId = intent.getIntExtra("imageResId", 0)

        // 데이터 뷰에 설정
        tvTitle.text = title
        tvContent.text = content
        tvUsername.text = username
        tvDogBreed.text = dogbreed
        tvDate.text = date
        tvLikeCount.text = likeCount.toString()
        tvCommentCount.text = commentCount.toString()

        // 이미지 리소스가 있다면 표시, 없으면 숨김 처리 (여기서는 첫 번째 이미지 뷰 사용)
        if (imageResId != 0) {
            ivPostImage1.setImageResource(imageResId)
            ivPostImage1.visibility = View.VISIBLE
        } else {
            ivPostImage1.visibility = View.GONE
        }
    }
}
