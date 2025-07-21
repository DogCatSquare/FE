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

        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

            // 로그용 인터셉터
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })

            // 자동 토큰 갱신 인터셉터 추가 ✅
            .addInterceptor(AuthInterceptor(context, tokenManager))

            // 재시도 인터셉터 (3번까지)
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

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}