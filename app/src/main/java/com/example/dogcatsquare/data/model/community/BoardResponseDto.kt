package com.example.dogcatsquare.data.model.community

import com.google.gson.annotations.SerializedName

data class BoardResponseDto(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: com.example.dogcatsquare.data.model.community.BoardData
)
