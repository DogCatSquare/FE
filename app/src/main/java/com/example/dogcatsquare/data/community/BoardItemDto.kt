package com.example.dogcatsquare.data.community

data class BoardItemDto(
    val id: Int,
    val boardName: String,
    val content: String,
    val keywords: List<String>,
    val createdAt: String?
)
