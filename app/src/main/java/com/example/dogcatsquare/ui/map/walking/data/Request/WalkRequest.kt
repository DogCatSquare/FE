package com.example.dogcatsquare.ui.map.walking.data.Request

data class WalkRequest(
    val title: String,
    val description: String,
    val time: Int,
    val distance: Int,
    val difficulty: String,
    val special: List<Special>,
    val coordinates: List<Coordinate>
)

data class Special(
    val type: String,
    val customValue: String? = null
)

data class Coordinate(
    val latitude: Double,
    val longitude: Double,
    val radius: Int
)

