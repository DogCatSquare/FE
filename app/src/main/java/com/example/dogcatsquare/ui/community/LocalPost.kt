package com.example.dogcatsquare.ui.community

data class LocalPost(
    val id: String,
    val username: String,
    val dogbreed: String,
    val images: List<Int>,
    val content: String
)
