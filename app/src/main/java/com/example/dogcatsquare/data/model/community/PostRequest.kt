package com.example.dogcatsquare.data.model.community

import com.google.gson.annotations.SerializedName

data class PostRequest(
    val boardId: Int,
    val title: String,
    val content: String,
    @SerializedName("videoUrl") val videoUrl: String?,
    @SerializedName("createdAt") val createdAt: String
)