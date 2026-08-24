package com.dogcatsquare.dogcatsquare.data.model.pet

import com.google.gson.annotations.SerializedName

data class AddPetRequest(
    @SerializedName("petName") var petName: String,
    @SerializedName("dogCat") var dogCat: String,
    @SerializedName("breed") var breed: String,
    @SerializedName("birth") var birth: String
)
