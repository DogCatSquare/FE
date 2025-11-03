package com.example.dogcatsquare.ui.map.walking.data

data class WalkCreateRequestDto(
    val coordinates: List<LatLngDto>,
    val duration: Long,
    val distance: Float,
    val description: String
)

// data/Request/LatLngDto.kt 파일 생성
data class LatLngDto(
    val latitude: Double,
    val longitude: Double
)

// data/Response/WalkCreateResponse.kt 파일 생성
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