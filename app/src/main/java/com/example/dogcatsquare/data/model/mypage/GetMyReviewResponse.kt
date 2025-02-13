package com.example.dogcatsquare.data.model.mypage

import com.google.gson.annotations.SerializedName

data class GetMyReviewResponse(
    @SerializedName("isSuccess") var isSuccess: Boolean,
    @SerializedName("code") var code: String,
    @SerializedName("message") var message: String,
    @SerializedName("result") var result: List<MyReviewResult>
)
data class MyReviewResult(
    val id: Int,
    val name: String,
    val content: String?,
    val createdAt: String,
    val placeId: Int?,
    val walkId: Int?
)
