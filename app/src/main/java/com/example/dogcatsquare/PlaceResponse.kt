package com.example.dogcatsquare

import com.google.gson.annotations.SerializedName

data class PlacesResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: List<PlaceItem>
)

data class PlaceItem(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("category") val category: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("distance") val distance: Double,
    @SerializedName("open") val open: Boolean,
    @SerializedName("regionId") val regionId: Int
)

data class PlacesData(
    val places: List<Place>
)

data class Place(
    val id: Int,
    val name: String,
    val address: String,
    val category: String,
    val distance: Double,
    val open: Boolean,
    val regionId: Int,
    val img_url: String,
    val longitude: Double,
    val latitude: Double
)

data class RegionRequest(
    @SerializedName("regionId")
    val regionId: Int,

    @SerializedName("do")
    val do_: String,

    @SerializedName("si")
    val si: String,

    @SerializedName("gu")
    val gu: String
)

data class PlaceRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("phoneNum")
    val phoneNum: String,

    @SerializedName("open")
    val open: Boolean,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("latitude")
    val latitude: Double
)

data class BaseResponse(
    @SerializedName("status")
    val status: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: Any?
)

data class SearchPlacesRequest(
    @SerializedName("userId") val userId: Int,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("latitude") val latitude: Double
)