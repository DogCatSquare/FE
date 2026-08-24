package com.dogcatsquare.dogcatsquare.data.model.login

import com.google.gson.annotations.SerializedName

data class VerifyResponse(
    @SerializedName("email") val email: String,
    @SerializedName("verified") val verified: Boolean
)
