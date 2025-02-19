package com.example.dogcatsquare.data.model.login

data class DeleteUserResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: String
)
