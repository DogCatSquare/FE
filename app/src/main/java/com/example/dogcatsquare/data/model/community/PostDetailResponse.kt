package com.example.dogcatsquare.data.model.community

data class PostDetailResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: com.example.dogcatsquare.data.model.community.PostDetail
)

