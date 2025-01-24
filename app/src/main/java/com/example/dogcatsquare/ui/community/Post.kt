package com.example.dogcatsquare.ui.community

import android.graphics.Bitmap

data class Post(
    val username: String,
    val dogbreed: String,
    val title: String?,
    val content: String?,
    val date: String,
    val thumbnailResId: Int?,
    val likeCount: Int = 0,
    val commentCount: Int = 0
)
