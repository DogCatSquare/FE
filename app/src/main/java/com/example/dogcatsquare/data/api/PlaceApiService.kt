package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.community.ApiResponse
import com.example.dogcatsquare.data.map.BaseResponse
import com.example.dogcatsquare.data.map.GetHotPlaceRequest
import com.example.dogcatsquare.data.map.GetHotPlaceResponse
import com.example.dogcatsquare.data.map.PageResponse
import com.example.dogcatsquare.data.map.PlaceDetailRequest
import com.example.dogcatsquare.data.map.PlaceDetailResponse
import com.example.dogcatsquare.data.map.PlaceItem
import com.example.dogcatsquare.data.map.PlaceUserInfoRequest
import com.example.dogcatsquare.data.map.SearchPlacesRequest
import com.example.dogcatsquare.data.map.WalkListRequest
import com.example.dogcatsquare.data.map.WalkListResponse
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface PlacesApiService {
    @POST("api/places/nearby")
    suspend fun searchPlaces(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Body request: SearchPlacesRequest
    ): BaseResponse<PageResponse<PlaceItem>>

    @POST("/api/places/search")
    suspend fun searchPlaces(
        @Header("Authorization") token: String,
        @Query("keyword") keyword: String,
        @Query("page") page: Int,
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
    fun getHotPlace(@Header("Authorization") token: String, @Path("cityId") cityId: Long?, @Body hotPlaceRequest: GetHotPlaceRequest): Call<GetHotPlaceResponse>

    @POST("api/walks")
    suspend fun getWalkList(
        @Header("Authorization") token: String,
        @Body request: WalkListRequest
    ): BaseResponse<WalkListResponse>

    @POST("api/places/{placeId}/data")
    suspend fun updatePlaceUserInfo(
        @Header("Authorization") token: String,
        @Path("placeId") placeId: Int,
        @Body request: PlaceUserInfoRequest
    ): BaseResponse<String>
}