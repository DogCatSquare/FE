package com.example.dogcatsquare.data.community

data class LikeResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: String
)
