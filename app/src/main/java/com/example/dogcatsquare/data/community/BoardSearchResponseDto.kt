package com.example.dogcatsquare.data.community

data class BoardSearchResponseDto(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<BoardItem>
)

data class BoardItem(
    val id: Int,
    val boardName: String,
    val content: String,
    val keywords: List<String>,
    val createdAt: String
)
