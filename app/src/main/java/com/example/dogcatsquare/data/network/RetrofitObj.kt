package com.example.dogcatsquare.data.network

import AuthInterceptor
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.dogcatsquare.data.local.TokenManager
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitObj {
    // base url
    private const val BASE_URL = "http://3.39.188.10:8080/"

    fun getRetrofit(context: Context): Retrofit {
        val tokenManager = TokenManager(context)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

            // ✅ 1. LoggingInterceptor 먼저 추가
            .addInterceptor(loggingInterceptor)

            // ✅ 2. AccessToken 헤더 추가
            .addInterceptor(AuthInterceptor(tokenManager))

            // ✅ 3. 401 Unauthorized 처리
            .authenticator(AuthAuthenticator(tokenManager))

            // ✅ 4. refresh token 만료 시 로그인 화면으로
            .addInterceptor(ResponseInterceptor(context))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}