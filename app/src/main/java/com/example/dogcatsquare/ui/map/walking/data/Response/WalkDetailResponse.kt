package com.example.dogcatsquare.ui.map.walking.data.Response

data class WalkDetailResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: WalkDetail
)

data class WalkDetail(
    val walkId: Long,
    val title: String,
    val description: String,
    val walkImageUrl: List<String>,
    val time: Int,
    val distance: Double,
    val difficulty: String,
    val special: List<Special>,
    val startCoordinates: List<Coordinate>,
    val endCoordinates: List<Coordinate>,
    val createdAt: String,
    val updatedAt: String,
    val createdBy: CreatedBy
)

data class Special(
    val type: String,
    val customValue: String?
)

data class Coordinate(
    val latitude: Double,
    val longitude: Double,
    val sequence: Int
)

data class CreatedBy(
    val nickname: String,
    val breed: String?,
    val profileImageUrl: String
)

