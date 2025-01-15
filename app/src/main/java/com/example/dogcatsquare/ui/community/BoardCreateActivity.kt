package com.example.dogcatsquare.ui.board

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dogcatsquare.databinding.ActivityBoardCreateBinding

class BoardCreateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBoardCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로가기 버튼 처리
        binding.ivBack.setOnClickListener {
            finish()
        }

        // '완료' 버튼 클릭 이벤트 처리
        binding.btnComplete.setOnClickListener {
            // TODO: 게시판 생성 로직 추가
        }
    }
}
