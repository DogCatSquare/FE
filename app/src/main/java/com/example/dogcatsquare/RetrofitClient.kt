package com.example.dogcatsquare

import android.util.Log
import com.example.dogcatsquare.data.api.PlacesApiService
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://3.39.188.10:8080"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .method(original.method, original.body)
                .build()

            try {
                chain.proceed(request)
            } catch (e: Exception) {
                Log.e("RetrofitClient", "Network request failed", e)
                throw e
            }
        }
        .connectTimeout(15, TimeUnit.SECONDS)      // 연결 타임아웃 감소
        .readTimeout(15, TimeUnit.SECONDS)         // 읽기 타임아웃 감소
        .writeTimeout(15, TimeUnit.SECONDS)        // 쓰기 타임아웃 감소
        .retryOnConnectionFailure(true)           // 연결 실패시 재시도
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    val placesApiService: PlacesApiService by lazy {
        retrofit.create(PlacesApiService::class.java)
    }
}