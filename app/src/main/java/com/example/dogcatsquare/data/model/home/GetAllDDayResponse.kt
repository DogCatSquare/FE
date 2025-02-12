package com.example.dogcatsquare.data.model.home

import com.google.gson.annotations.SerializedName

data class GetAllDDayResponse(
    @SerializedName("isSuccess") var isSuccess: Boolean,
    @SerializedName("code") var code: String,
    @SerializedName("message") var message: String,
    @SerializedName("result") var result: List<GetAllDDayResult>
)
data class GetAllDDayResult(
    @SerializedName("id") var id: Int,
    @SerializedName("title") var title: String,
    @SerializedName("day") var day: String,
    @SerializedName("term") var term: Int?,
    @SerializedName("daysLeft") var daysLeft: Int,
    @SerializedName("isAlarm") var isAlarm: Boolean,
    @SerializedName("ddayText") var ddayText: String,
    @SerializedName("ddayImageUrl") var ddayImageUrl: String
)
