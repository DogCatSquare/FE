package com.example.dogcatsquare.data.map

data class MapPlace(
    var placeName: String? = "",
    var placeType: String? = "",
    var placeDistance: String? = "",
    var placeLocation: String? = "",
    var placeCall: String? = "",
    var char1Text: String? = null,
    var char2Text: String? = null,
    var char3Text: String? = null,
    var placeImg: Int? = null,
    var placeReview: String? = null,
    var longitude: Double? = null,
    var latitude: Double? = null,
    var isOpen: String? = null
)