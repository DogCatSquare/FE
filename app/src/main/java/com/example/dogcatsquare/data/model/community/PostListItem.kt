package com.example.dogcatsquare.data.model.community

import com.google.gson.annotations.SerializedName

data class PostListItem(
    val id: Int,
    @SerializedName("boardType") val boardType: String,
    val username: String,
    @SerializedName("animal_type") val animalType: String?,
    val title: String,
    val content: String,
    @SerializedName("videoUrl") val videoUrl: String?,
    @SerializedName("video_URL") val videoUrlSnake: String?,
    @SerializedName("thumbnail_URL") val thumbnailUrl: String?,
    @SerializedName("profileImage_URL") val profileImageUrl: String?,
    val images: List<String>?,
    @SerializedName("like_count") val likeCount: Int,
    @SerializedName("comment_count") val commentCount: Int,
    val createdAt: String,
    val userId: Int
)