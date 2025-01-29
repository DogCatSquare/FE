package com.example.dogcatsquare.data.community

data class BoardResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: BoardData?
)

data class BoardData(
    val id: Int,
    val boardName: String,
    val content: String,
    val keywords: List<String>,
    val createdAt: String
)
