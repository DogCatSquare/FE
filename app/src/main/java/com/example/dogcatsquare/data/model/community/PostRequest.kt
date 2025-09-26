package com.example.dogcatsquare.data.model.community

import com.google.gson.annotations.SerializedName

data class PostRequest(
    @SerializedName("boardType") val boardType: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("videoUrl") val videoUrl: String? = null
)