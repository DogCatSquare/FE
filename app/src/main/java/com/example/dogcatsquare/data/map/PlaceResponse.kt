package com.example.dogcatsquare.data.map

import com.google.gson.annotations.SerializedName

data class PlaceItem(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("category") val category: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("distance") val distance: Double,
    @SerializedName("open") val open: Boolean,
    @SerializedName("imgUrl") val imgUrl: String,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("reviewCount") val reviewCount: Int
)

data class PageResponse<T>(
    @SerializedName("content") val content: List<T>,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("totalElements") val totalElements: Int,
    @SerializedName("last") val last: Boolean,
    @SerializedName("first") val first: Boolean,
    @SerializedName("size") val size: Int,
    @SerializedName("number") val number: Int
)



data class BaseResponse<T>(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: T?
)

data class SearchPlacesRequest(
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("latitude") val latitude: Double
)

data class PlaceDetailResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("category") val category: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("open") val open: Boolean,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("businessHours") val businessHours: String?,
    @SerializedName("homepageUrl") val homepageUrl: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("facilities") val facilities: List<String>?,
    @SerializedName("imageUrls") val imageUrls: List<String>?,
    @SerializedName("distance") val distance: Double,
    @SerializedName("wished") val wished: Boolean,
    @SerializedName("recentReviews") val recentReviews: List<MapReview>?
)

data class PlaceDetailRequest(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)