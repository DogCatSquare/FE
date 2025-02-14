package com.example.dogcatsquare.data.community

data class BoardData(
    val id: Int,
    val boardName: String,
    val content: String,
    val keywords: List<String>,
    val createdAt: String
)