package com.example.dogcatsquare.data.community

data class BoardData(
    val id: Int,
    val boardName: String,
    val content: String,
    val keyword: List<String>,
    val createdAt: String
)