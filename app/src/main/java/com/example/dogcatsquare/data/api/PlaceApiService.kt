package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.map.BaseResponse
import com.example.dogcatsquare.data.map.PlaceItem
import com.example.dogcatsquare.data.map.PlaceRequest
import com.example.dogcatsquare.data.map.RegionRequest
import com.example.dogcatsquare.data.map.SearchPlacesRequest
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Path

interface PlacesApiService {
    @POST("api/regions")
    suspend fun createRegion(
        @Header("Authorization") token: String,
        @Body region: RegionRequest
    ): BaseResponse<Int>

    @POST("api/regions/{regionId}/places")
    suspend fun createPlace(
        @Header("Authorization") token: String,
        @Path("regionId") regionId: Int,
        @Body place: PlaceRequest
    ): BaseResponse<Int>

    @POST("api/regions/{regionId}/places/search")
    suspend fun searchPlaces(
        @Header("Authorization") token: String,
        @Path("regionId") regionId: Int,
        @Body request: SearchPlacesRequest
    ): BaseResponse<List<PlaceItem>>
}