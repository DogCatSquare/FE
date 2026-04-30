package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.model.mypage.GetMyPostResponse
import com.example.dogcatsquare.data.model.mypage.GetMyReviewResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.DELETE

interface MyPageRetrofitItf {
    @GET("api/myReviews")
    fun getMyReview(@Header("Authorization") token: String, @Query("page") page: Int): Call<GetMyReviewResponse>

    @DELETE("api/myReviews/{reviewId}")
    fun deleteMyReview(
        @Header("Authorization") token: String,
        @Path("reviewId") reviewId: Int,
        @Query("type") type: String
    ): Call<com.example.dogcatsquare.data.model.common.BaseResponse<Void>>

    @GET("api/board/posts/user/{userId}")
    fun getMyPost(@Header("Authorization") token: String, @Path("userId") userId: Int): Call<GetMyPostResponse>
}