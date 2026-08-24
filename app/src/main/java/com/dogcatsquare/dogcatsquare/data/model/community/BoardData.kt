package com.dogcatsquare.dogcatsquare.data.model.community

data class BoardData(
    val id: Int,
    val boardName: String,
    val content: String,
    val keywords: List<String>?,
    val createdAt: String
)