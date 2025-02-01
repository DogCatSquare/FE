package com.example.dogcatsquare.data.pet

import com.google.gson.annotations.SerializedName

data class AddPetRequest(
    @SerializedName("petName") var petName: String,
    @SerializedName("docCat") var dogCat: String,
    @SerializedName("breed") var breed: String,
    @SerializedName("birth") var birth: String
)
