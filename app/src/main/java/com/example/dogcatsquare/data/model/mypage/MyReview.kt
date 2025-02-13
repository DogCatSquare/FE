package com.example.dogcatsquare.data.model.mypage

data class MyReview(
    val content: List<ReviewContent>,
    val totalPages: Int,
    val totalElements: Int,
    val last: Boolean,
    val first: Boolean,
    val size: Int,
    val number: Int
)

data class ReviewContent(
    val id: Int,
    val content: String,
    val createdAt: String,
    val imageUrls: List<String>?
)