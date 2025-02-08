package com.example.dogcatsquare.data.model.home

import com.google.gson.annotations.SerializedName

data class AddDDayRequest(
    @SerializedName("title") var title: String,
    @SerializedName("day") var day: String,
    @SerializedName("term") var term: Int
)
