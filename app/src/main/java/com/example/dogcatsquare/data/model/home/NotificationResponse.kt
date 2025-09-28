package com.example.dogcatsquare.data.model.home

data class NotificationResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: NotificationResult
)

data class NotificationResult(
    val ddayId: Int,
    val userId: Int,
    val enabled: Boolean,
    val startDate: String, // "yyyy-MM-dd"
    val scheduledAt: String, // "yyyy-MM-ddTHH:mm:ss.sssZ" (서버 예약 시간)
    val reservationCreated: Boolean,
    val canceledCount: Int
)
