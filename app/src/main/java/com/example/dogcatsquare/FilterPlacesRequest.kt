package com.example.dogcatsquare

data class FilterPlacesRequest(
    val location: Location,
    val is24Hours: Boolean,
    val hasParking: Boolean,
    val isCurrentlyOpen: Boolean
) {
    data class Location(
        val latitude: Double,
        val longitude: Double
    )
}