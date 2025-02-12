package com.example.dogcatsquare.data.model.login

import com.google.gson.annotations.SerializedName

data class SignUpRequest(
    @SerializedName("email") var email: String,
    @SerializedName("password") var password: String,
    @SerializedName("nickname") var nickname: String,
    @SerializedName("phoneNumber") var phoneNumber: String,
    @SerializedName("doName") var doName: String,
    @SerializedName("si") var si: String,
    @SerializedName("gu") var gu: String,
    @SerializedName("pet") var pet: Pet,
    @SerializedName("foodDate") var foodDate: String,
    @SerializedName("foodDuring") var foodDuring: Int,
    @SerializedName("padDate") var padDate: String,
    @SerializedName("padDuring") var padDuring: Int,
    @SerializedName("hospitalDate") var hospitalDate: String,
    @SerializedName("adAgree") var adAgree: Boolean
)
data class Pet(
    @SerializedName("petName") var petName: String,
    @SerializedName("dogCat") var dogCat: String,
    @SerializedName("breed") var breed: String,
    @SerializedName("birth") var birth: String
)
enum class DogCat {
    DOG,
    CAT
}