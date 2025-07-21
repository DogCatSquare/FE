package com.example.dogcatsquare.data.model.community

data class PostResponse(
    val id: Int,
    val board: String,
    val title: String,
    val content: String,
    val video_URL: String?,
    val thumbnail_URL: String?,
    val like_count: Int,
    val comment_count: Int,
    val createdAt: String,
    val images: List<String>?
)