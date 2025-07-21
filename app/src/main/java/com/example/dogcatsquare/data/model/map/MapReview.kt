package com.example.dogcatsquare.data.model.map

data class MapReview(
    val id: Int,
    val content: String?,
    val breed: String?,
    val nickname: String?,
    val userImageUrl: String?,
    val createdAt: String?,
    val userId: Int,
    val placeReviewImageUrl: List<String>?,
    val placeId: Int
)
