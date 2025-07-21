package com.example.dogcatsquare.data.model.community

data class BoardRequestDto(
    val boardName: String,
    val content: String,
    val keywords: List<String>?
)
