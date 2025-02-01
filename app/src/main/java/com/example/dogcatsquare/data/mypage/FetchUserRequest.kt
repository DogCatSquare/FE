package com.example.dogcatsquare.data.mypage

import com.google.gson.annotations.SerializedName

data class FetchUserRequest(
    @SerializedName("nickname") var nickname: String?,
    @SerializedName("phoneNumber") var phoneNumber: String?,
    @SerializedName("password") var password: String?
)
