package com.example.dogcatsquare.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상단바 색깔
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        checkLogin()
    }

    // 로그인 체크 -> 추후 수정
    private fun checkLogin() {
        val intent = Intent(this, LoginDetailActivity::class.java)
        startActivity(intent)
        finish()
    }
}