package com.example.dogcatsquare.data.model.pet

import com.google.gson.annotations.SerializedName

data class FetchPetRequest(
    @SerializedName("petName") var petName: String?,
    @SerializedName("dogCat") var dogCat: String?,
    @SerializedName("breed") var breed: String?,
    @SerializedName("birth") var birth: String?
)
