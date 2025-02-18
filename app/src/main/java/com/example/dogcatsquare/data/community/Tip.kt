package com.example.dogcatsquare.data.community

data class Tip(
    val title: String,
    val content: String,
    val thumbnailResId: Int,
    val nickname: String,
    val time: String,
    val likeCount: Int,
    val commentCount: Int,
    val dogBreed: String,
    val date: String
)
