package com.example.dogcatsquare.data.community

data class PostRequest(
    val boardId: Int,
    val title: String,
    val content: String,
    val video_URL: String,
    val created_at: String
)