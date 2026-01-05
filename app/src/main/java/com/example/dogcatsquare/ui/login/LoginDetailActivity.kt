package com.example.dogcatsquare.ui.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.dogcatsquare.MainActivity
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.local.TokenManager
import com.example.dogcatsquare.data.model.login.LoginRequest
import com.example.dogcatsquare.data.model.login.LoginResponse
import com.example.dogcatsquare.data.model.login.RefreshTokenResponse
import com.example.dogcatsquare.databinding.ActivityLoginDetailBinding
import com.example.dogcatsquare.utils.FcmUtils.updateFcmToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginDetailActivity: AppCompatActivity() {
    lateinit var binding: ActivityLoginDetailBinding
    private lateinit var tokenManager: TokenManager
    private var loginChecked : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상단바 색깔
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        tokenManager = TokenManager(this)

        // signup btn click
        binding.signupBtn.setOnClickListener {
            navigateToSignup()
        }

        // EditText 값 변경 감지
        setupTextWatchers()

        // if autoLogin checked
        if (tokenManager.isAutoLogin()) {
            binding.loginEmailEt.setText(tokenManager.getEmail())
            binding.loginPwEt.setText(tokenManager.getPassword())
            binding.checkBoxAll.isChecked = true

            handleLogin(tokenManager.getEmail() ?: "", tokenManager.getPassword() ?: "")
        }

        binding.loginBtn.setOnClickListener {
            val email = binding.loginEmailEt.text.toString()
            val pw = binding.loginPwEt.text.toString()

            if (email.isEmpty() || pw.isEmpty()) {
                binding.errorTv.visibility = View.VISIBLE
                binding.errorTv.text = "이메일과 비밀번호를 입력하세요"
            } else {
                // 로그인 처리
                // 만약 자동로그인 체크되어있으면 정보 저장
                if (loginChecked) {
                    tokenManager.setAutoLogin(true)
                    tokenManager.saveUserInfo(-1, email, pw, -1) // 나중에 서버 응답값으로 덮어씀
                }
                handleLogin(email, pw)
            }
        }

        // autoLogin checkBox click
        binding.checkBoxAll.setOnCheckedChangeListener{ _, isChecked ->
//            if (isChecked) {
//                loginChecked = true
//            } else {
//                loginChecked = false
//                editor.clear()
//                editor.commit()
//            }
            loginChecked = isChecked
            if (!isChecked) tokenManager.clear()
        }
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
        val loginService = RetrofitObj.getRetrofit(this).create(UserRetrofitItf::class.java)
        loginService.login(LoginRequest(email, password)).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.code() == 200 && response.body()?.isSuccess == true) {
                    val result = response.body()!!.result
                    tokenManager.saveTokens(result.token, result.refreshToken)
                    tokenManager.saveUserInfo(result.userId, result.email, password, result.cityId)
                    updateFcmToken(this@LoginDetailActivity)
                    navigateToMain()
                } else {
                    binding.errorTv.visibility = View.VISIBLE
                    binding.errorTv.text = "로그인 정보가 일치하지 않습니다"
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("Login/FAILURE", "서버 오류: ${t.message}")
            }
        })
    }

    private fun saveUserInfo(token: String, refreshToken: String, id: Int, email: String, pw: String, cityId: Long) {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()){
            putString("token", token)
            putString("refreshToken", refreshToken)
            putInt("userId", id) // 아이디 값 전달
            putString("email", email)
            putString("pw", pw)
            putLong("cityId", cityId)
            apply()
        }
    }

    // 회원가입으로
    private fun navigateToSignup() {
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
    }

    // 메인으로
//    private fun navigateToMain(loginResponse: LoginResponse) {
//        saveUserInfo(
//            loginResponse.result.token,
//            loginResponse.result.refreshToken,
//            loginResponse.result.userId,
//            loginResponse.result.email,
//            binding.loginPwEt.text.toString(),
//            loginResponse.result.cityId
//        )
//
//        val intent = Intent(this, MainActivity::class.java)
//        startActivity(intent)
//        finish() // 로그인 화면 종료
//    }
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
