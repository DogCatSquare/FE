package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.model.home.GetAllEventsResponse
import retrofit2.Call
import retrofit2.http.GET

interface EventRetrofitItf {
    @GET("api/events")
    fun getAllEvents(): Call<GetAllEventsResponse>
}