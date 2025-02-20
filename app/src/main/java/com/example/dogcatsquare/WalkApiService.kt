package com.example.dogcatsquare

import retrofit2.http.GET
import retrofit2.http.Query

interface WalkApiService {
    @GET("/api/v1/walks/search")
    suspend fun searchWalks(
        @Query("title") title: String
    ): WalkSearchResponse
}