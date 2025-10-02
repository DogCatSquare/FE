package com.example.dogcatsquare.data.model.home

data class RegisterFcmRequest(
    val userId: Int,
    val fcmToken: String
)
