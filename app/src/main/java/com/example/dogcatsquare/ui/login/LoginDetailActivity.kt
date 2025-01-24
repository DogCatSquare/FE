package com.example.dogcatsquare.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.dogcatsquare.MainActivity
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.ActivityLoginDetailBinding

class LoginDetailActivity: AppCompatActivity() {
    lateinit var binding: ActivityLoginDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상단바 색깔
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        // 로그인 상태 확인
//        if (isLoggedIn()) {
//            navigateToMain()
//            return
//        }

        // EditText 값 변경 감지
        setupTextWatchers()

        // 로그인 버튼 클릭 이벤트
        binding.loginBtn.setOnClickListener {
            val id = binding.loginIdEt.text.toString()
            val password = binding.loginPwEt.text.toString()

            if (id.isEmpty() || password.isEmpty()) {
                // 아무것도 안함
            } else {
                // 로그인 처리
                handleLogin(id, password)
            }
        }

        // 카카오 로그인 버튼 클릭 이벤트
        binding.kakaoLoginBtn.setOnClickListener {
            Toast.makeText(this, "카카오 로그인 시도 중...", Toast.LENGTH_SHORT).show()
            handleKakaoLogin()
        }

        // 아이디가 없으면 회원가입(임시)
        binding.signupBtn.setOnClickListener {
            navigateToSignup()
        }

        // 자동 로그인 체크박스
//        binding.checkBoxAll.setOnCheckedChangeListener { _, isChecked ->
//            saveLoginState(isChecked)
//        }
    }

    private fun setupTextWatchers() {
        val loginBtn = binding.loginBtn

        val updateButtonState: () -> Unit = {
            val id = binding.loginIdEt.text.toString()
            val password = binding.loginPwEt.text.toString()

            // ID와 비밀번호가 모두 입력되었는지 확인
            val isEnabled = id.isNotEmpty() && password.isNotEmpty()
            loginBtn.isEnabled = isEnabled

            // 버튼 색상 업데이트
            if (isEnabled) {
                loginBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.main_color1)) // 활성화 색상
                loginBtn.setTextColor(ContextCompat.getColor(this, R.color.white)) // 텍스트 색상
            } else {
                loginBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.gray1)) // 비활성화 색상
                loginBtn.setTextColor(ContextCompat.getColor(this, R.color.gray4)) // 텍스트 색상
            }
        }

        // ID 입력 필드 감지
        binding.loginIdEt.addTextChangedListener { updateButtonState() }

        // 비밀번호 입력 필드 감지
        binding.loginPwEt.addTextChangedListener { updateButtonState() }
    }

    // 로그인
    private fun handleLogin(id: String, password: String) {
        // 예시: 간단한 로그인 검증 (실제 구현에서는 서버 통신 필요)
        if (id == "testuser" && password == "1234") {
            Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
            // 자동 로그인 구현



            // 로그인 성공 후 메인 화면으로 이동
            navigateToMain()
        } else {
            Toast.makeText(this, "아이디 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 자동 로그인
    private fun saveLoginState(isLoggedIn: Boolean) {
        val sharedPreferences = getSharedPreferences("AutoLoginPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.apply()
    }

    private fun isLoggedIn(): Boolean {
        val sharedPreferences = getSharedPreferences("AutoLoginPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    // 카카오 로그인
    private fun handleKakaoLogin() {
        // 카카오 로그인 로직 구현
        // 예시: 성공 메시지
        Toast.makeText(this, "카카오 로그인 성공!", Toast.LENGTH_SHORT).show()
        navigateToMain()
    }

    // 회원가입으로
    private fun navigateToSignup() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
    }

    // 메인으로
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // 로그인 화면 종료
    }
}