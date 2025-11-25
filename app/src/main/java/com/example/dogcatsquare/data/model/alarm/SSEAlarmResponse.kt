package com.example.dogcatsquare.data.model.alarm

import com.google.gson.annotations.SerializedName

data class SSEAlarmResponse(
    @SerializedName("timeout") val timeout: Int
)
