package com.example.dogcatsquare.data.model.home

import com.google.gson.annotations.SerializedName

data class FetchDDayRequest(
    @SerializedName("day") var day: String,
    @SerializedName("term") var term: Int,
    @SerializedName("isAlarm") var isAlarm: Boolean
)
