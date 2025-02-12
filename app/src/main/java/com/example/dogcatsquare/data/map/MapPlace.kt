package com.example.dogcatsquare.data.map

data class MapPlace(
    val id: Int,
    var placeName: String? = "",
    var placeType: String? = "",
    var placeDistance: String? = "",
    var placeLocation: String? = "",
    var placeCall: String? = "",
    var placeImgUrl: String? = null,
    var isOpen: String? = null,
    val reviewCount: Int? = null
)