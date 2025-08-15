package com.example.dogcatsquare.data.model.post

data class Post(
    val id: Int,
    val board: String,
    var username: String,
    val title: String,
    val content: String,
    val video_URL: String?,
    val thumbnail_URL: String?,
    var like_count: Int,
    val comment_count: Int,
    val createdAt: String,
    val profileImage_URL: String?,
    val images: List<String?>?
)
