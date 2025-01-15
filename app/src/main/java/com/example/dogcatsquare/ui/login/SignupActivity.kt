package com.example.dogcatsquare.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dogcatsquare.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 다음 버튼 클릭 시 반려동물 정보 입력 화면으로 넘어가기
        binding.signupNextBtn.setOnClickListener {
            checkSignup()
        }
    }

    private fun checkSignup() {
        val intent = Intent(this, SignupPetInfoActivity::class.java)
        startActivity(intent)
    }
}