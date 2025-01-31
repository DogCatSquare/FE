package com.example.dogcatsquare.data.community

data class BoardResponseDto(
    val status: Int,
    val message: String,
    val data: BoardData
)