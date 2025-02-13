package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.map.BaseResponse
import com.example.dogcatsquare.data.map.GetHotPlaceRequest
import com.example.dogcatsquare.data.map.GetHotPlaceResponse
import com.example.dogcatsquare.data.map.PageResponse
import com.example.dogcatsquare.data.map.PlaceDetailRequest
import com.example.dogcatsquare.data.map.PlaceDetailResponse
import com.example.dogcatsquare.data.map.PlaceItem
import com.example.dogcatsquare.data.map.SearchPlacesRequest
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface PlacesApiService {
    @POST("api/places/search/{cityId}")
    suspend fun searchPlaces(
        @Header("Authorization") token: String,
        @Path("cityId") cityId: Int,
        @Query("keyword") keyword: String,
        @Body request: SearchPlacesRequest
    ): BaseResponse<PageResponse<PlaceItem>>

    @POST("api/places/{placeId}")
    suspend fun getPlaceById(
        @Header("Authorization") token: String,
        @Path("placeId") placeId: Int,
        @Body request: PlaceDetailRequest
    ): BaseResponse<PlaceDetailResponse>

    @POST("api/wishlist/places/{placeId}")
    suspend fun toggleWish(
        @Header("Authorization") token: String,
        @Path("placeId") placeId: Int
    ): BaseResponse<Boolean>

    @POST("api/places/hot/{cityId}")
    fun getHotPlace(@Header("Authorization") token: String, @Path("cityId") cityId: Int, @Body hotPlaceRequest: GetHotPlaceRequest): Call<GetHotPlaceResponse>
}