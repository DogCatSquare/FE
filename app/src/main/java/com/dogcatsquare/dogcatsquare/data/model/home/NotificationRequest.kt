package com.dogcatsquare.dogcatsquare.data.model.home

data class NotificationRequest(
    val startDate: String,
    val termWeeks: Int,
    val enabled: Boolean
)
