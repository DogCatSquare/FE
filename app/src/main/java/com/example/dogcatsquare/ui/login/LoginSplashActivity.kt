package com.example.dogcatsquare.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.dogcatsquare.MainActivity
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.local.TokenManager
import com.example.dogcatsquare.data.model.login.RefreshTokenResponse
import com.example.dogcatsquare.data.network.RetrofitClient
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.ActivityLoginSplashBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginSplashActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginSplashBinding
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginSplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        // 상단바 색깔
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        checkLogin()
    }

    private fun checkLogin() {
        val refreshToken = tokenManager.getRefreshToken()

        // refreshToken이 없으면 로그인 화면으로 이동
        if (refreshToken.isNullOrEmpty()) {
            moveToLoginDetail()
            return
        }

        // refreshToken으로 accessToken 갱신 요청
        val userApiService = RetrofitObj.getRetrofit(this).create(UserRetrofitItf::class.java)
        userApiService.refreshToken("Bearer $refreshToken").enqueue(object :
            Callback<RefreshTokenResponse> {
            override fun onResponse(call: Call<RefreshTokenResponse>, response: Response<RefreshTokenResponse>) {
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val result = response.body()!!.result
                    tokenManager.saveTokens(result.accessToken, result.refreshToken)
                    moveToMain()
                } else {
                    Log.e("LoginActivity", "토큰 갱신 실패: ${response.body()?.message}")
                    moveToLoginDetail()
                }
            }

            override fun onFailure(call: Call<RefreshTokenResponse>, t: Throwable) {
                Log.e("LoginActivity", "서버 오류: ${t.message}")
                moveToLoginDetail()
            }
        })
    }

    private fun moveToLoginDetail() {
        val intent = Intent(this, LoginDetailActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun moveToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}