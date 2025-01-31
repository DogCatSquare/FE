package com.example.dogcatsquare

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
        @Path("regionId") regionId: Int,
        @Body place: PlaceRequest
    ): BaseResponse<Unit>
}