package com.example.dogcatsquare.data.model.login

import com.google.gson.annotations.SerializedName

data class SendVerficationRequest(
    @SerializedName("email") val email: String
)
