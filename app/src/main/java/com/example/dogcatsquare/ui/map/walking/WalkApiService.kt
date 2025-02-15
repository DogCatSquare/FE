package com.example.dogcatsquare.ui.map.walking

import com.example.dogcatsquare.ui.map.walking.data.Request.WalkRequest
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkDetailResponse
import com.example.dogcatsquare.ui.map.walking.data.Request.WalkReviewRequest
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkResponse
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkReviewResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface WalkApiService {

    // 산책로 등록
    @POST("/api/walks")
    fun registerWalkRoute(@Body walkRequest: WalkRequest): Call<WalkResponse>

    // 특정 산책로 상세 조회
    @GET("/api/walks/{walkId}")
    fun getWalkDetail(@Path("walkId") walkId: Long): Call<WalkDetailResponse>

    // 산책로 후기 등록
    @POST("/api/walks/{walkId}/reviews")
    @Headers("Content-Type: application/json")
    fun submitWalkReview(
        @Path("walkId") walkId: Long,
        @Body reviewRequest: WalkReviewRequest
    ): Call<WalkReviewResponse>


}

