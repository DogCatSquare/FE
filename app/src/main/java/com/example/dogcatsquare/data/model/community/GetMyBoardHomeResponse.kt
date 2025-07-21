package com.example.dogcatsquare.data.model.community

data class GetMyBoardHomeResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<String>
)
