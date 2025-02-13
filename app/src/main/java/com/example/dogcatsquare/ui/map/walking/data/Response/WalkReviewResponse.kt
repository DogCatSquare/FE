package com.example.dogcatsquare.ui.map.walking.data.Response

data class WalkReviewResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: WalkReviewResult
)

data class WalkReviewResult(
    val walkReviews: List<WalkReview>
)

data class WalkReview(
    val reviewId: Long,
    val walkId: Long,
    val content: String,
    val walkReviewImageUrl: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val createdBy: User
)

data class User(
    val nickname: String,
    val breed: String,
    val profileImageUrl: String
)

