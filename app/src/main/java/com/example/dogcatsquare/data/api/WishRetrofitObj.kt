package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.map.MyLocation
import com.example.dogcatsquare.data.model.wish.FetchMyWishPlaceResponse
import com.example.dogcatsquare.data.model.wish.GetMyWishResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WishRetrofitObj {
    @POST("api/places/wishlist")
    fun getMyWish(@Header("Authorization") token: String, @Query("page") page: Int, @Body myLocation: MyLocation): Call<GetMyWishResponse>

    @POST("api/wishlist/places/{placeId}")
    fun fetchMyWishPlaceList(@Header("Authorization") token: String, @Path("placeId") placeId: Int): Call<FetchMyWishPlaceResponse>
}