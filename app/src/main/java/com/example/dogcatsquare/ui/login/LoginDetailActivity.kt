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
import com.example.dogcatsquare.data.model.login.LoginRequest
import com.example.dogcatsquare.data.model.login.LoginResponse
import com.example.dogcatsquare.databinding.ActivityLoginDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginDetailActivity: AppCompatActivity() {
    lateinit var binding: ActivityLoginDetailBinding

    lateinit var pref : SharedPreferences
    lateinit var editor : SharedPreferences.Editor
    private var loginChecked : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        editor = pref.edit()

        // 상단바 색깔
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        // signup btn click
        binding.signupBtn.setOnClickListener {
            navigateToSignup()
        }

        // EditText 값 변경 감지
        setupTextWatchers()

        // if autoLogin checked
        if (pref.getBoolean("autoLogin", false)) {
            binding.loginEmailEt.setText(pref.getString("email", ""))
            binding.loginPwEt.setText(pref.getString("pw", ""))
            binding.checkBoxAll.isChecked = true

            val savedEmail = binding.loginEmailEt.text.toString().trim()
            val savedPw = binding.loginPwEt.text.toString().trim()

            handleLogin(savedEmail, savedPw)
        } else {
            // 로그인 버튼 클릭 이벤트
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
                        editor.putString("email", email)
                        editor.putString("pw", pw)
                        editor.putBoolean("autoLogin", true)
                        editor.commit()
                    }
                    handleLogin(email, pw)
                }
            }
        }

        // autoLogin checkBox click
        binding.checkBoxAll.setOnCheckedChangeListener{ buttonView, isChecked ->
            if (isChecked) {
                loginChecked = true
            } else {
                loginChecked = false
                editor.clear()
                editor.commit()
            }
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
                        binding.errorTv.text = "로그인 정보가 일치하지 않습니다"
                        Log.d("Login/FAILURE", "존재하지 않는 이메일")
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }

        })
    }

    private fun saveUserInfo(token: String, id: Int, email: String, pw: String) {
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()){
            putString("token", token)
            putInt("userId", id) // 아이디 값 전달
            putString("email", email)
            putString("pw", pw)
            apply()
        }
    }

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

        var email: String = loginResponse.result.email
        var pw: String = binding.loginPwEt.text.toString()

        saveUserInfo(token, id, email, pw)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // 로그인 화면 종료
    }
}
