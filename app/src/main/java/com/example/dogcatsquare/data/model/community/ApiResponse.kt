package com.example.dogcatsquare.data.model.community

data class ApiResponse(
    val status: Int,
    val message: String,
    val data: com.example.dogcatsquare.data.model.community.PostResponse?
)
