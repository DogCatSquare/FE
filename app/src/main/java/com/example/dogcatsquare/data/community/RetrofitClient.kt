package com.example.dogcatsquare.api

import com.example.dogcatsquare.RetrofitClient.retrofit
import com.example.dogcatsquare.data.api.BoardApiService
import com.example.dogcatsquare.data.api.UserRetrofitItf
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://3.39.188.10:8080/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val instance: BoardApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(BoardApiService::class.java)
    }

    val userApiService: UserRetrofitItf by lazy {
        retrofit.create(UserRetrofitItf::class.java)
    }
}
