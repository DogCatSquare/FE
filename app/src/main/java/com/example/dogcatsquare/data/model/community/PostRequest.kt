package com.example.dogcatsquare.data.model.community

import com.google.gson.annotations.SerializedName

data class PostRequest(
    val boardId: Int,
    val title: String,
    val content: String,
    @SerializedName("videoUrl") val video_URL: String?,
    @SerializedName("createdAt") val created_at: String
)
