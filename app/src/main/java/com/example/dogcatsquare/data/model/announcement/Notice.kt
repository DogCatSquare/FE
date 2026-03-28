package com.example.dogcatsquare.data.model.announcement

import com.google.gson.annotations.SerializedName

data class Notice(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("createdAt") val createdAt: String
)
