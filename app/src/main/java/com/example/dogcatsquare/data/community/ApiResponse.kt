package com.example.dogcatsquare.data.community

data class ApiResponse(
    val status: Int,
    val message: String,
    val data: PostResponse?
)
