package com.example.dogcatsquare.data.model.community

data class Tip(
    val title: String,
    val content: String,
    val thumbnailUrl: String? = null,
    val videoUrl: String? = null,
    val nickname: String,
    val time: String,
    val likeCount: Int,
    val commentCount: Int,
    val dogBreed: String,
    val date: String,
    val thumbnailResId: Int = 0
)