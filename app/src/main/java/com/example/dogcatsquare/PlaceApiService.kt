package com.example.dogcatsquare

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.Query  // Query import 추가

interface PlacesApiService {
    @POST("api/regions")
    suspend fun createRegion(@Body region: RegionRequest): BaseResponse

    @POST("api/regions/{regionId}/places")
    suspend fun createPlace(
        @Path("regionId") regionId: Int,
        @Body place: PlaceRequest
    ): BaseResponse

    // POST로 변경하고 요청 본문 추가
    @POST("api/regions/{regionId}/places/search")
    suspend fun getAllPlaces(
        @Path("regionId") regionId: Int,
        @Body request: SearchPlacesRequest
    ): PlacesResponse
}