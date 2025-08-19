package com.example.dogcatsquare.data.community

import com.example.dogcatsquare.data.model.community.PostDetail

data class PostDetailResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: PostDetail,
)

