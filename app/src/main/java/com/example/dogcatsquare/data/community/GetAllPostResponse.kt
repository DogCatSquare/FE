package com.example.dogcatsquare.data.community

import com.google.gson.annotations.SerializedName

data class GetAllPostResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<GetAllPostResult>
)
data class GetAllPostResult(
    @SerializedName("id")
    val id: Int,

    @SerializedName("board")
    val board: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("animal_type")
    val animal_type: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("video_URL")
    val videoURL: String?,

    @SerializedName("thumbnail_URL")
    val thumbnailURL: String?,

    @SerializedName("profileImage_URL")
    val profileImageURL: String?,

    @SerializedName("images")
    val images: List<String>?,

    @SerializedName("like_count")
    val likeCount: Int,

    @SerializedName("comment_count")
    val commentCount: Int,

    @SerializedName("createdAt")
    val createdAt: String
)