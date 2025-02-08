package com.example.dogcatsquare.data.model.pet

import com.google.gson.annotations.SerializedName

data class DeletePetResponse(
    @SerializedName("isSuccess") var isSuccess: Boolean,
    @SerializedName("code") var code: String,
    @SerializedName("message") var message: String,
    @SerializedName("result") var result: String
)
