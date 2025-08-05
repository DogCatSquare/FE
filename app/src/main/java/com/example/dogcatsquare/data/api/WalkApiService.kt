package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.model.walk.WalkSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WalkApiService {
    @GET("/api/walks/search")  // 엔드포인트 경로 수정
    suspend fun searchWalks(
        @Query("title") title: String  // parameter 이름은 title 유지
    ): WalkSearchResponse
}