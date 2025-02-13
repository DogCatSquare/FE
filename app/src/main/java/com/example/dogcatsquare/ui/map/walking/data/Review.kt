package com.example.dogcatsquare.ui.map.walking.data

data class Review(
    val profileImageUrl: String,
    val nickname: String,
    val breed: String?,
    val reviewContent: String,
    val reviewDate: String,
    val reviewImageUrl: String?,
)