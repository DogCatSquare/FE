package com.example.dogcatsquare.data.community

data class BoardRequestDto(
    val boardName: String,
    val content: String,
    val keyword: List<String>
)