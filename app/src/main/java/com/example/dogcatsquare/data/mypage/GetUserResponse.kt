package com.example.dogcatsquare.data.mypage

import android.text.Editable

data class GetUserResponse(
    var isSuccess: Boolean,
    var code: String,
    var message: String,
    var result: GetUserResult
)
data class GetUserResult(
    var id: Int,
    var email: String,
    var nickname: String,
    var phoneNumber: String,
    var doName: String,
    var si: String,
    var gu: String,
    var adAgree: Boolean,
    var firstPetBreed: String,
    var profileImageUrl: String
)
