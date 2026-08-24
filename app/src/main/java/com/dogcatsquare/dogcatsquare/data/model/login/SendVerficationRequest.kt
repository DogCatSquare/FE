package com.dogcatsquare.dogcatsquare.data.model.login

import com.google.gson.annotations.SerializedName

data class SendVerficationRequest(
    @SerializedName("email") val email: String
)
