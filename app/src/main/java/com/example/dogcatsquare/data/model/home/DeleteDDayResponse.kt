package com.example.dogcatsquare.data.model.home

import com.google.gson.annotations.SerializedName

data class DeleteDDayResponse(
    @SerializedName("isSuccess") var isSuccess: Boolean,
    @SerializedName("code") var code: String,
    @SerializedName("message") var message: String
)
