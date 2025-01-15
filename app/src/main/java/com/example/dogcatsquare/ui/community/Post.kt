package com.example.dogcatsquare.ui.community

import android.graphics.Bitmap

data class Post(
    val username: String,
    val location: String,
    val title: String?,
    val content: String?,
    val date: String,
    val thumbnail: Bitmap?, // 썸네일 이미지 (null이면 회색 배경)
    val likeCount: Int = 0,
    val commentCount: Int = 0
)
