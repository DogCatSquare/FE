package com.example.dogcatsquare.data.model.mypage

data class MyReview(
    val id: Int,
    val name: String,
    val content: String?,
    val createdAt: String,
    val placeId: Int?,
    val walkId: Int?
//    val image_urls: List<String>
)
