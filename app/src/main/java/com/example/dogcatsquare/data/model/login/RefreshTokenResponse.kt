package com.example.dogcatsquare.data.model.login

import com.google.gson.annotations.SerializedName

data class RefreshTokenResponse(
    @SerializedName("isSuccess") var isSuccess: Boolean,
    @SerializedName("code") var code: String,
    @SerializedName("message") var message: String,
    @SerializedName("result") var result: RefreshTokenResult
)
data class RefreshTokenResult(
    @SerializedName("accessToken") var accessToken: String,
    @SerializedName("refreshToken") var refreshToken: String
)
