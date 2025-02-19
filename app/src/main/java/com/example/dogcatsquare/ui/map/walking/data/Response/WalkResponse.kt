package com.example.dogcatsquare.ui.map.walking.data.Response

data class WalkResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: WalkResult
)

data class WalkResult(
    val walks: List<Walk>
)

data class Walk(
    val walkId: Int,
    val title: String,
    val description: String,
    val walkImageUrl: List<String>,
    val reviewCount: Int,
    val distance: Int,
    val time: Int,
    val difficulty: String,
    val special: List<SpecialFeature>,
    val coordinates: List<Coordinate>,
    val createdAt: String,
    val updatedAt: String,
    val createdBy: CreatedBy
)

data class SpecialFeature(
    val type: String,
    val customValue: String
)


