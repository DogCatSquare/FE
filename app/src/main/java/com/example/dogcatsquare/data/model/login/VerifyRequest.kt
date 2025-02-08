package com.example.dogcatsquare.data.model.login

import com.google.gson.annotations.SerializedName

data class VerifyRequest(
    @SerializedName("email") val email: String,
    @SerializedName("verificationCode") val code: String
)
