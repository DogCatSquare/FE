package com.example.dogcatsquare

import com.google.gson.annotations.SerializedName

data class PlaceItem(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("category") val category: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("distance") val distance: Double,
    @SerializedName("open") val open: Boolean,
    @SerializedName("regionId") val regionId: Int,
    @SerializedName("imgUrl") val imgUrl: String,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("latitude") val latitude: Double
)

data class RegionRequest(
    @SerializedName("doName")
    val doName: String,

    @SerializedName("si")
    val si: String,

    @SerializedName("gu")
    val gu: String
)

data class PlaceRequest(
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("category") val category: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("open") val open: Boolean,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("businessHours") val businessHours: String? = null,
    @SerializedName("homepageUrl") val homepageUrl: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("facilities") val facilities: List<String>? = null
)

data class BaseResponse<T>(
    @SerializedName("isSuccess")
    val isSuccess: Boolean,

    @SerializedName("code")
    val code: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("result")
    val result: T?
)

data class SearchPlacesRequest(
    @SerializedName("userId") val userId: Int,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("keyword") val keyword: String
)