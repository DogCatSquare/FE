package com.example.dogcatsquare.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dogcatsquare.MainActivity
import com.example.dogcatsquare.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 회원가입 텍스트 클릭 시 회원가입 화면으로 넘어가기
        binding.goSignupBtn.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // 임시로 로그인 버튼 클릭 시 메인 화면으로
        binding.loginBtn.setOnClickListener {
            checkLogin()
        }
    }

    // 로그인 체크 -> 추후 수정
    private fun checkLogin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}