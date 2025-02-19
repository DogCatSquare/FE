package com.example.dogcatsquare

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface NaverGeocodeService {
    @GET("map-geocode/v2/geocode")
    suspend fun geocode(@Query("query") address: String): NaverGeocodeResponse
}

data class NaverGeocodeResponse(
    @SerializedName("addresses") val addresses: List<Address>
)

data class Address(
    @SerializedName("x") val longitude: String,
    @SerializedName("y") val latitude: String
)