package com.example.dogcatsquare.data.community

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://your-api-url.com" // 실제 API 주소로 변경

    val instance: BoardApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(BoardApiService::class.java)
    }
}
