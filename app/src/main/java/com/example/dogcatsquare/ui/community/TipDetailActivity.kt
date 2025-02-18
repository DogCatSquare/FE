package com.example.dogcatsquare.ui.community

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dogcatsquare.R

class TipDetailActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var ivUserProfile: ImageView
    private lateinit var tvUserNickname: TextView
    private lateinit var tvDogBreed: TextView
    private lateinit var tvTipTitle: TextView
    private lateinit var ivLikeButton: ImageView
    private lateinit var ivPostMenu: ImageView
    private lateinit var ivTipThumbnail: ImageView
    private lateinit var tvTipContent: TextView
    private lateinit var tvPostDate: TextView
    private lateinit var tvLikeCount: TextView
    private lateinit var tvCommentCount: TextView

    private var tipId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 상세보기 레이아웃으로 item_tips.xml 사용
        setContentView(R.layout.activity_tip_detail)

        // 뒤로가기 버튼
        ivBack = findViewById(R.id.ivBack)
        ivBack.setOnClickListener { finish() }

        // 나머지 뷰 초기화
        ivUserProfile = findViewById(R.id.ivUserProfile)
        tvUserNickname = findViewById(R.id.tvUserNickname)
        tvDogBreed = findViewById(R.id.tvDogBreed)
        tvTipTitle = findViewById(R.id.tvTipTitle)
        ivLikeButton = findViewById(R.id.ivLikeButton)
        ivPostMenu = findViewById(R.id.ivPostMenu)
        ivTipThumbnail = findViewById(R.id.ivTipThumbnail)
        tvTipContent = findViewById(R.id.tvTipContent)
        tvPostDate = findViewById(R.id.tvPostDate)
        tvLikeCount = findViewById(R.id.tvLikeCount)
        tvCommentCount = findViewById(R.id.tvCommentCount)

        // Intent로부터 데이터 받기
        tipId = intent.getLongExtra("tipId", -1L)
        val titleExtra = intent.getStringExtra("title") ?: "제목 없음"
        val contentExtra = intent.getStringExtra("content") ?: "내용 없음"
        val thumbnailResId = intent.getIntExtra("thumbnailResId", 0)
        val nicknameExtra = intent.getStringExtra("nickname") ?: "닉네임 없음"
        val dogBreedExtra = intent.getStringExtra("dogBreed") ?: "견종 없음"
        val dateExtra = intent.getStringExtra("date") ?: "날짜 없음"
        val likeCountExtra = intent.getIntExtra("likeCount", 0)
        val commentCountExtra = intent.getIntExtra("commentCount", 0)

        // 메뉴 버튼 클릭 시 PopupMenu (수정/삭제 옵션)
        ivPostMenu.setOnClickListener { view ->
            val popup = PopupMenu(this, view)
            popup.menuInflater.inflate(R.menu.post_menu, popup.menu)
            popup.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.menu_edit -> {
                        // 수정 화면으로 전환
                        val editIntent = Intent(this, EditPostActivity::class.java).apply {
                            putExtra("tipId", tipId)
                            putExtra("title", titleExtra)
                            putExtra("content", contentExtra)
                            // 필요한 추가 데이터 전달
                        }
                        startActivity(editIntent)
                        true
                    }
                    R.id.menu_delete -> {
                        // 삭제 후 결과 전달 (로컬 삭제 방식)
                        val resultIntent = Intent().apply {
                            putExtra("DELETED_TIP_ID", tipId)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        Toast.makeText(this, "삭제 완료", Toast.LENGTH_SHORT).show()
                        finish()  // 상세 화면 종료
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        // 데이터 할당
        tvTipTitle.text = titleExtra
        tvTipContent.text = contentExtra
        tvUserNickname.text = nicknameExtra
        tvDogBreed.text = dogBreedExtra
        tvPostDate.text = dateExtra
        tvLikeCount.text = likeCountExtra.toString()
        tvCommentCount.text = commentCountExtra.toString()

        if (thumbnailResId != 0) {
            ivTipThumbnail.setImageResource(thumbnailResId)
        }
    }
}
