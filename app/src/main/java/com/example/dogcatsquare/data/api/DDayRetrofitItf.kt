package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.model.home.AddDDayRequest
import com.example.dogcatsquare.data.model.home.AddDDayResponse
import com.example.dogcatsquare.data.model.home.DeleteDDayResponse
import com.example.dogcatsquare.data.model.home.FetchDDayRequest
import com.example.dogcatsquare.data.model.home.FetchDDayResponse
import com.example.dogcatsquare.data.model.home.GetAllDDayResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DDayRetrofitItf {
    @POST("api/ddays")
    fun addDDay(@Header("Authorization") token: String, @Body addDDayRequest: AddDDayRequest): Call<AddDDayResponse>

    @GET("api/ddays")
    fun getAllDDays(@Header("Authorization") token: String): Call<GetAllDDayResponse>

    @DELETE("api/ddays/{ddayId}")
    fun deleteDDay(@Header("Authorization") token: String): Call<DeleteDDayResponse>

    @PUT("api/ddays/{ddayId}")
    fun fetchDDay(@Header("Authorization") token: String, @Path("ddayId") ddayId: Int, @Body fetchDDayRequest: FetchDDayRequest): Call<FetchDDayResponse>
}