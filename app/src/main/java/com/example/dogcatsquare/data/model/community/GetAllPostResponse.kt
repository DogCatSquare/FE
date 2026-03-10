package com.example.dogcatsquare.data.model.community

data class GetAllPostResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<PostListItem>
)