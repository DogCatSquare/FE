package com.example.dogcatsquare.data.model.walk

data class WalkSearchResponse(
    val walks: List<Walk>  // result가 아닌 walks로 변경
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