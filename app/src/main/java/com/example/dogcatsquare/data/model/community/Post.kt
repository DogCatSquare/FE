package com.example.dogcatsquare.data.model.community

import com.google.gson.annotations.SerializedName

data class Post(
    val id: Int,
    val board: String,
    val username: String,
    val dogbreed: String,
    val title: String,
    val content: String,
    val videoUrl: String?,
    val thumbnailUrl: String?,
    val profileImageUrl: String?,
    val images: List<String>?,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: String
)

data class PostListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<com.example.dogcatsquare.data.model.community.Post>
)