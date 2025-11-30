package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.model.alarm.ReadNotificationRequest
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AlarmApiService {
    @POST("/api/notification/read")
    suspend fun markNotificationsAsRead(@Body request: ReadNotificationRequest): Response<Unit>
}
