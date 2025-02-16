package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.map.DeleteReviewResponse
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.Path

interface ReviewRetrofitItf {
    @DELETE("api/walks/{walkId}/reviews/{reviewId}")
    fun deleteWalkReview(@Header("Authorization") token: String, @Path("walkId") walkId: Int, @Path("reviewId") reviewId: Int): Call<DeleteReviewResponse>

    @DELETE("api/places/{placeId}/reviews/{reviewId}")
    fun deletePlaceReview(@Header("Authorization") token: String, @Path("placeId") placeId: Int, @Path("reviewId") reviewId: Int): Call<DeleteReviewResponse>
}