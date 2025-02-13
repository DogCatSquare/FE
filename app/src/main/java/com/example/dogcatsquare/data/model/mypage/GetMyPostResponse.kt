package com.example.dogcatsquare.data.model.mypage

import com.google.gson.annotations.SerializedName

data class GetMyPostResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: List<MyPostResult>
)
data class MyPostResult(
    @SerializedName("id") val id: Int,
    @SerializedName("board") val board: String,
    @SerializedName("username") val username: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("video_URL") val videoUrl: String?,
    @SerializedName("thumbnail_URL") val thumbnailUrl: String?,
    @SerializedName("profileImage_URL") val profileImageUrl: String?,
    @SerializedName("images") val images: List<String?>?,
    @SerializedName("like_count") val likeCount: Int,
    @SerializedName("comment_count") val commentCount: Int,
    @SerializedName("createdAt") val createdAt: String
)
