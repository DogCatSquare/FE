package com.example.dogcatsquare.data.model.community

data class PostResponse(
    val id: Int?,
    val boardType: String?,
    val username: String?,
    val animal_type: String?,
    val title: String?,
    val content: String?,
    val video_URL: String?,
    val thumbnail_URL: String?,
    val profileImage_URL: String?,
    val images: List<String>?,
    val like_count: Int?,
    val comment_count: Int?,
    val createdAt: String?
)