package com.example.dogcatsquare.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.dogcatsquare.MainActivity
import com.example.dogcatsquare.R
import com.example.dogcatsquare.RetrofitObj
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.login.CheckNicknameResponse
import com.example.dogcatsquare.data.login.LoginRequest
import com.example.dogcatsquare.data.login.LoginResponse
import com.example.dogcatsquare.databinding.ActivityLoginDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
            val email = binding.loginEmailEt.text.toString()
            val password = binding.loginPwEt.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                // 아무것도 안함
            } else {
                // 로그인 처리
                handleLogin(email, password)
            }
        }

        // 카카오 로그인 버튼 클릭 이벤트
        binding.kakaoLoginBtn.setOnClickListener {
            Toast.makeText(this, "카카오 로그인 시도 중...", Toast.LENGTH_SHORT).show()
//            handleKakaoLogin()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
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
            val email = binding.loginEmailEt.text.toString()
            val password = binding.loginPwEt.text.toString()

            // ID와 비밀번호가 모두 입력되었는지 확인
            val isEnabled = email.isNotEmpty() && password.isNotEmpty()
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
        binding.loginEmailEt.addTextChangedListener {
            if(binding.errorTv.visibility == View.VISIBLE) {
                binding.errorTv.visibility = View.INVISIBLE
            }
            updateButtonState()
        }

        // 비밀번호 입력 필드 감지
        binding.loginPwEt.addTextChangedListener { updateButtonState() }
    }

    // 로그인
    private fun handleLogin(email: String, password: String) {
        val loginService = RetrofitObj.getRetrofit().create(UserRetrofitItf::class.java)
        loginService.login(LoginRequest(email, password)).enqueue(object : Callback<LoginResponse>{
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                Log.d("Login/SUCCESS", response.toString())

                when(response.code()) {
                    200 -> {
                        val resp: LoginResponse = response.body()!!
                        if (resp != null) {
                            if (resp.isSuccess) {
                                navigateToMain(resp)
                            } else {
                                Log.e("Login/FAILURE", "응답 코드: ${resp.code}, 응답 메시지: ${resp.message}")
                            }
                        } else {
                            Log.d("Login/FAILURE", "Response body is null")
                            Log.e("Login/FAILURE", "응답 코드: ${resp.code}, 응답메시지: ${resp.message}")
                        }
                    }
                    500 -> {
                        binding.errorTv.visibility = View.VISIBLE
                        Log.d("Login/FAILURE", "존재하지 않는 이메일")
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }

        })
    }

    // 토큰을 SharedPreferences에 저장
    private fun saveToken(token: String){
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()){
            putString("token", token)
            apply()
        }
    }

    private fun saveId(id: Int){
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()){
            putInt("UserId", id) // 아이디 값 전달
            apply()
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
//    private fun handleKakaoLogin() {
//        // 카카오 로그인 로직 구현
//        // 예시: 성공 메시지
//        Toast.makeText(this, "카카오 로그인 성공!", Toast.LENGTH_SHORT).show()
//        navigateToMain()
//    }

    // 회원가입으로
    private fun navigateToSignup() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
    }

    // 메인으로
    private fun navigateToMain(loginResponse: LoginResponse) {
        Log.d("message", loginResponse.message)
        Log.d("액세스 토큰 값", loginResponse.result.token)

        // 로그인 연동 후 받은 토큰 값 저장
        var token: String = loginResponse.result.token
        Log.d("토큰 값", token)

        // 로그인 연동 후 받은 아이디 저장
        var id: Int = loginResponse.result.userId
        Log.d("Nickname액티비티 사용자 아이디 값", id.toString())

        saveToken(token)
        saveId(id)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // 로그인 화면 종료
    }
}