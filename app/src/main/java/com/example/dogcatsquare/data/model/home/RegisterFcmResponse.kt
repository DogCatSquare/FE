package com.example.dogcatsquare.data.model.home

data class RegisterFcmResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: Nothing? = null
)
