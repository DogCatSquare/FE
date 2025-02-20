package com.example.dogcatsquare.data.model.post

import com.google.gson.annotations.SerializedName

data class PopularPostResponse(
    @SerializedName("isSuccess") var isSuccess: Boolean,
    @SerializedName("code") var code: String,
    @SerializedName("message") var message: String,
    @SerializedName("result") var result: List<PopularPopularResult>
)
data class PopularPopularResult(
    @SerializedName("id") var id: Int,
    @SerializedName("board") var board: String,
    @SerializedName("username") var username: String,
    @SerializedName("animal_type") var animal_type: String,
    @SerializedName("title") var title: String,
    @SerializedName("content") var content: String,
    @SerializedName("video_URL") var video_URL: String?,
    @SerializedName("thumbnail_URL") var thumbnail_URL: String?,
    @SerializedName("profuleImage_URL") var profileImage_URL: String?,
    @SerializedName("like_count") var like_count: Int,
    @SerializedName("comment_count") var comment_count: Int,
    @SerializedName("createdAt") var createdAt: String,
    @SerializedName("images") var images: List<String>?
)
