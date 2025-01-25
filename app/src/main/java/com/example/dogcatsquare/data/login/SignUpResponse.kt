package com.example.dogcatsquare.data.login

data class SignUpResponse(
    var isSuccess: Boolean,
    var code: String,
    var message: String,
    var result: SignUpResult
)
data class SignUpResult(
    var id: Int,
    var email: String,
    var nickname: String,
    var phoneNumber: String,
    var regionId: String
)
