package com.example.dogcatsquare.data.map

import com.google.gson.annotations.SerializedName

data class GetHotPlaceRequest(
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("latitude") val latitude: Double
)
