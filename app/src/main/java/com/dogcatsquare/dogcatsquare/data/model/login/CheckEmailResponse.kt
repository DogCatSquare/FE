package com.dogcatsquare.dogcatsquare.data.model.login

data class CheckEmailResponse(
    var isSuccess: Boolean,
    var code: String,
    var message: String,
    var result: Boolean
)
