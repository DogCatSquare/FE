package com.example.dogcatsquare.data.community

data class PostDetailResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: PostDetail
)

