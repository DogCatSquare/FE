package com.example.dogcatsquare.data.model.community

data class LocalPost(
    val id: Long,
    val username: String,
    val dogbreed: String,
    val title: String?,
    val content: String?,
    val videoUrl: String?,
    val thumbnailUrl: String?,
    val images: List<Any>
)
