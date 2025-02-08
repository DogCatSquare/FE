package com.example.dogcatsquare.data.model.login

data class CheckNicknameResponse(
    var isSuccess: Boolean,
    var code: String,
    var message: String,
    var result: Boolean
)
