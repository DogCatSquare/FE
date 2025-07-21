package com.example.dogcatsquare.data.model.map

data class MapPlace(
    val id: Int,
    var placeName: String? = "",
    var placeType: String? = "",
    var placeDistance: String? = "",
    var placeLocation: String? = "",
    var placeCall: String? = "",
    var placeImgUrl: String? = null,
    var isOpen: String? = null,
    val reviewCount: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val keywords: List<String>? = null,

    // 산책로 전용 필드
    val walkTime: Int? = null,
    val walkDifficulty: String? = null,
    val walkSpecial: List<Special>? = null,
    val walkCoordinates: List<Coordinate>? = null,
    val createdBy: CreatedBy? = null
)