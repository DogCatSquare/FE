package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.model.common.BaseResponse
import com.example.dogcatsquare.data.model.walk.WalkSearchResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface WalkApiService {

    @GET("/api/walks/search")
    suspend fun searchWalks(
        @Query("title") title: String
    ): WalkSearchResponse

    @DELETE("/api/walks/{walkId}")
    suspend fun deleteWalk(
        @Header("Authorization") token: String,
        @Path("walkId") walkId: Int
    ): BaseResponse<Unit>
}