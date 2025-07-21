package com.example.dogcatsquare.data.model.community

data class BoardPost(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<com.example.dogcatsquare.data.model.community.PostDetail>?
)