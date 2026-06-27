package com.example.dogcatsquare.data.model.map

import com.google.gson.annotations.SerializedName

data class RecommendPlaceRequest(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)
