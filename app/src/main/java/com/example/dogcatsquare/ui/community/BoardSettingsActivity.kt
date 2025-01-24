package com.example.dogcatsquare.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dogcatsquare.databinding.ActivityBoardSettingsBinding

class BoardSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBoardSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로가기 버튼 클릭 이벤트
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 게시판 만들기 버튼 클릭 이벤트
        binding.btnCreateBoard.setOnClickListener {
            val intent = Intent(this, com.example.dogcatsquare.ui.board.BoardCreateActivity::class.java)
            startActivity(intent)
        }


        // 게시판 검색 로직
        binding.etSearchBoard.setOnEditorActionListener { v, actionId, event ->
            val searchQuery = binding.etSearchBoard.text.toString()
            // TODO: 검색 로직 추가
            true
        }


    }
}
