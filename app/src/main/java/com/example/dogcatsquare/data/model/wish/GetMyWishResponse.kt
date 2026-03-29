package com.example.dogcatsquare.data.model.wish

import com.google.gson.annotations.SerializedName

data class GetMyWishResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<MyWishContent>
)

data class MyWishContent(
    @SerializedName("googlePlaceId") val googlePlaceId: String,
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
    val walks: List<MyWishWalkContent>?
)

data class MyWishWalkContent(
    val walkId: Long,
    val title: String,
    val distance: Double,
    val time: Int,
    val walkImageUrl: List<String>
)
