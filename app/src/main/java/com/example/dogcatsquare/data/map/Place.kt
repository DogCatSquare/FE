package com.example.dogcatsquare.data.map

data class Place(
    var id: Int,
    var name: String,
    var address: String?,
    var category: String,
    var phoneNumber: String?,
    var longitude: Double?,
    var latitude: Double?,
    var distance: Double,
    var open: Boolean,
    var imgUrl: String?,
    var reviewCount: Int?
)
