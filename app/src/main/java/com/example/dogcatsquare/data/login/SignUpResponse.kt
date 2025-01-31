package com.example.dogcatsquare.data.login

import com.google.gson.annotations.SerializedName

data class SignUpResponse(
    @SerializedName("isSuccess") var isSuccess: Boolean,
    @SerializedName("code") var code: String,
    @SerializedName("message") var message: String,
    @SerializedName("result") var result: SignUpResult
)
data class SignUpResult(
    @SerializedName("id") var id: Int,
    @SerializedName("email") var email: String,
    @SerializedName("nickname") var nickname: String,
    @SerializedName("phoneNumber") var phoneNumber: String,
    @SerializedName("regionId") var regionId: String,
    @SerializedName("profileImageUrl") var profileImageUrl: String,
    @SerializedName("token") var token: String
)
