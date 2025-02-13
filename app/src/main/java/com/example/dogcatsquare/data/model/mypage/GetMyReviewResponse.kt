package com.example.dogcatsquare.data.model.mypage

import com.google.gson.annotations.SerializedName

data class GetMyReviewResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: MyReviewResult
)

data class MyReviewResult(
    @SerializedName("content") val content: List<PostContent>,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("totalElements") val totalElements: Int,
    @SerializedName("last") val last: Boolean,
    @SerializedName("first") val first: Boolean,
    @SerializedName("size") val size: Int,
    @SerializedName("number") val number: Int
)

data class PostContent(
    @SerializedName("id") val id: Int,
    @SerializedName("content") val content: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("imageUrls") val imageUrls: List<String>?
)

