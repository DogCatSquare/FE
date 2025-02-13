package com.example.dogcatsquare.data.model.login

data class LoginResponse(
    var isSuccess: Boolean,
    var code: String,
    var message: String,
    var result: LoginResponseResult
)
data class LoginResponseResult(
    var token: String,
    var email: String,
    var nickname: String,
    var userId: Int,
    var city: String,
    var cityId: Long,
    var districtId: Long
)
