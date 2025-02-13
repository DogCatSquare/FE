package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.model.home.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface WeatherRetrofitItf {
    @GET("api/weather/current")
    fun getWeather(@Header("Authorization") token: String): Call<WeatherResponse>
}