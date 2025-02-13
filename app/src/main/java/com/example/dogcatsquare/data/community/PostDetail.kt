package com.example.dogcatsquare.data.community

import com.google.gson.annotations.SerializedName

data class PostDetail(
    val id: Long,
    val board: String,
    val username: String,
    val title: String,
    val content: String,
    @SerializedName("video_URL")
    val videoUrl: String,
    @SerializedName("thumbnail_URL")
    val thumbnailUrl: String,
    @SerializedName("profileImage_URL")
    val profileImageUrl: String,
    val images: List<String>,
    @SerializedName("like_count")
    val likeCount: Int,
    @SerializedName("comment_count")
    val commentCount: Int,
    @SerializedName("createdAt")
    val createdAt: String
)