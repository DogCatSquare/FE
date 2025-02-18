package com.example.dogcatsquare.data.network

import AuthInterceptor
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // 연결 타임아웃 증가
        .readTimeout(60, TimeUnit.SECONDS)    // 읽기 타임아웃 증가
        .writeTimeout(60, TimeUnit.SECONDS)   // 쓰기 타임아웃 증가
        .retryOnConnectionFailure(true)       // 연결 실패 시 재시도

        // 로그 인터셉터 추가 (요청 로그 확인)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })

        // 재시도 인터셉터 추가 (3번까지 재시도)
        .addInterceptor { chain ->
            var response: Response? = null
            var attempt = 0
            val maxAttempts = 3
            while (attempt < maxAttempts) {
                try {
                    response = chain.proceed(chain.request())
                    if (response.isSuccessful) break
                } catch (e: Exception) {
                    Log.e("OkHttp", "Request failed: ${e.message}")
                }
                attempt++
            }
            response ?: throw IOException("Failed after $maxAttempts attempts")
        }
        .build()

    fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}