package com.example.dogcatsquare.data.model.wish

data class GetMyWishResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: GetMyWishResult
)
data class GetMyWishResult(
    val content: List<MyWishContent>,
    val totalPages: Int,
    val totalElements: Int,
    val last: Boolean,
    val first: Boolean,
    val size: Int,
    val number: Int
)
data class MyWishContent(
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
    val keywords: List<String>?
)
