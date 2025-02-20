package com.example.dogcatsquare

data class WalkSearchResponse(
    val isSuccess: Boolean,
    val code: Int,
    val message: String,
    val result: List<Walk>  // walks가 아닌 result로 변경
)

data class WalksResult(
    val walks: List<Walk>
)

data class Walk(
    val walkId: Int,
    val title: String,
    val description: String,
    val walkImageUrl: List<String>,
    val reviewCount: Int,
    val distance: Double,
    val time: Int,
    val difficulty: String,
    val special: List<Special>,
    val coordinates: List<Coordinate>,
    val createdAt: String,
    val updatedAt: String,
    val createdBy: Creator
)

data class Special(
    val type: String,
    val customValue: String
)

data class Coordinate(
    val latitude: Double,
    val longitude: Double,
    val sequence: Int
)

data class Creator(
    val nickname: String,
    val breed: String,
    val profileImageUrl: String
)