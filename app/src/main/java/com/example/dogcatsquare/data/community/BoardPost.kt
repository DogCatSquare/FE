package com.example.dogcatsquare.data.community

data class BoardPost(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<PostDetail>?
)