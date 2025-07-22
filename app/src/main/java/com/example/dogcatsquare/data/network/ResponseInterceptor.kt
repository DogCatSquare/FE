package com.example.dogcatsquare.data.network

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.dogcatsquare.ui.login.LoginDetailActivity
import okhttp3.Interceptor
import okhttp3.Response

class ResponseInterceptor(
    private val context: Context
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 401) {
            Log.e("ResponseInterceptor", "401 Unauthorized 감지 → 로그인 이동")

            // 토큰 삭제
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()

            // 로그인 이동
            val intent = Intent(context, LoginDetailActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }

        return response
    }
}