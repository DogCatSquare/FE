package com.example.dogcatsquare.data.model.wish

data class WishPlace(
    val id: Int,
    val name: String,
    val address: String,
    val category: String,
    val phoneNumber: String?,
    val longitude: Double,
    val latitude: Double,
    val distance: Double,
    val open: Boolean?,
    val imgUrl: String?,
    val reviewCount: Int,
    val keywords: List<String>?,
    var isWish: Boolean
)
