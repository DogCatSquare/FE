package com.example.dogcatsquare.ui.map.walking.data

data class WalkCreateRequestDto(
    val title: String,
    val description: String,
    val time: Int,
    val distance: Int,
    val difficulty: String,
    val special: List<SpecialDto>,
    val coordinates: List<CoordinateDto>
)

data class SpecialDto(
    val type: String,
    val customValue: String
)

data class CoordinateDto(
    val latitude: Double,
    val longitude: Double,
    val sequence: Int
)

data class WalkCreateResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: WalkCreateResult
)

data class WalkCreateResult(
    val success: Boolean,
    val message: String,
    val walkId: Long
)