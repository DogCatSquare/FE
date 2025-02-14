package com.example.dogcatsquare.ui.map.walking.data.Response

data class WalkResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: WalkListResult
)

data class WalkListResult(
    val walks: List<WalkRoute>
)

data class WalkRoute(
    val walkId: Long,
    val title: String,
    val description: String,
    val walkImageUrl: List<String>,
    val reviewCount: Int,
    val distance: Double,
    val time: Int,
    val difficulty: String,
    val special: List<Special>,
    val coordinates: List<CoordinateData>,
    val createdAt: String,
    val updatedAt: String,
    val createdBy: CreatedBy
)

data class CoordinateData(
    val latitude: Double,
    val longitude: Double,
    val sequence: Int
)

data class Special(
    val type: String,
    val customValue: String?
)

data class CreatedBy(
    val nickname: String,
    val breed: String?,
    val profileImageUrl: String
)



