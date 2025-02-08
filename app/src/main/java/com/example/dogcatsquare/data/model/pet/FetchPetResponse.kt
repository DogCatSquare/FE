package com.example.dogcatsquare.data.model.pet

import com.google.gson.annotations.SerializedName

data class FetchPetResponse(
    @SerializedName("isSuccess") var isSuccess: Boolean,
    @SerializedName("code") var code: String,
    @SerializedName("message") var message: String,
    @SerializedName("result") var result: FetchPetResult
)
data class FetchPetResult(
    @SerializedName("id") var id: Int,
    @SerializedName("petName") var petName: String,
    @SerializedName("dogCat") var dogCat: String,
    @SerializedName("breed") var breed: String,
    @SerializedName("birth") var birth: String,
    @SerializedName("petImageUrl") var petImageUrl: String,
)