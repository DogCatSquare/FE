package com.example.dogcatsquare.ui.map.walking

import com.example.dogcatsquare.ui.map.walking.data.Request.Coordinate
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

    // 산책로 목록 조회
    @POST("/api/walks")
    fun getWalkList(@Body walkRequest: Coordinate): WalkResponse

    // 특정 산책로 상세 조회
    @GET("/api/walks/{walkId}")
    fun getWalkDetail(@Path("walkId") walkId: Int): Call<WalkDetailResponse>

    //산책로 후기 조회
    @GET("/api/walks/{walkId}/reviews")
    fun getWalkReview(@Path("walkId") walkId: Int): Call<WalkReviewResponse>


    // 산책로 후기 등록
    @POST("/api/walks/{walkId}/reviews")
    @Headers("Content-Type: application/json")
    fun submitWalkReview(
        @Path("walkId") walkId: Long,
        @Body reviewRequest: WalkReviewRequest
    ): Call<WalkReviewResponse>


}

