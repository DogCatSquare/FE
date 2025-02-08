package com.example.dogcatsquare.data.community

data class BoardListResponseDto(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<BoardData>
)
