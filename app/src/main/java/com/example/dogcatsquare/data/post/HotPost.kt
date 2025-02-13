package com.example.dogcatsquare.data.post

data class HotPost(
    val id: Int,
    val board: String,
    var username: String,
    val title: String,
    val content: String,
    val video_URL: String?,
    val thumbnail_URL: String?,
    val like_count: Int,
    val comment_count: Int,
    val createdAt: String,
    val images: List<String>?
)
