package com.example.dogcatsquare.data.model.community

import com.google.gson.annotations.SerializedName

data class PostCreateRequest(
    @SerializedName("boardType") val boardType: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("videoUrl") val videoUrlCamel: String? = null,
    @SerializedName("video_URL") val videoUrlSnake: String? = null
)