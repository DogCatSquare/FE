package com.example.dogcatsquare.data.model.home

import com.google.gson.annotations.SerializedName

data class GetAllEventsResponse(
    @SerializedName("isSuccess") var isSuccess: Boolean,
    @SerializedName("code") var code: String,
    @SerializedName("message") var message: String,
    @SerializedName("result") var result: List<GetAllEventsResult>
)
data class GetAllEventsResult(
    @SerializedName("id") var id: Int,
    @SerializedName("title") var title: String,
    @SerializedName("period") var period: String,
    @SerializedName("bannerImageUrl") var bannerImageUrl: String,
    @SerializedName("eventUrl") var eventUrl: String
)
