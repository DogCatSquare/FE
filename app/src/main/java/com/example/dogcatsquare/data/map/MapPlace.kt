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
    val placeImg: Int?,                 // 로컬 리소스 이미지 ID
    val placeImgUrl: String?,           // 서버에서 제공하는 이미지 URL
    var placeReview: String? = null,
    var longitude: Double? = null,
    var latitude: Double? = null,
    var isOpen: String? = null
)