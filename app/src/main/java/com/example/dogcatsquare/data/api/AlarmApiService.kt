package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.model.alarm.SSEAlarmResponse
import retrofit2.Call
import retrofit2.http.GET

interface AlarmApiService {
    @GET("/api/notification/sse/subscribe")
    fun subscribeToSseEvents(): Call<SSEAlarmResponse>
}
