package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.model.home.AddDDayRequest
import com.example.dogcatsquare.data.model.home.AddDDayResponse
import com.example.dogcatsquare.data.model.home.DeleteDDayResponse
import com.example.dogcatsquare.data.model.home.GetAllDDayResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface DDayRetrofitItf {
    @POST("api/ddays")
    fun addDDay(@Header("Authorization") token: String, @Body addDDayRequest: AddDDayRequest): Call<AddDDayResponse>

    @GET("api/ddays")
    fun getAllDDays(@Header("Authorization") token: String): Call<GetAllDDayResponse>

    @DELETE("api/ddays/{ddayId}")
    fun deleteDDay(@Header("Authorization") token: String): Call<DeleteDDayResponse>
}