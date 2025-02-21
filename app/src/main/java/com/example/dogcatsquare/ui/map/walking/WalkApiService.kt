package com.example.dogcatsquare.ui.map.walking

import com.example.dogcatsquare.ui.map.walking.data.Address
import com.example.dogcatsquare.ui.map.walking.data.Request.Coordinate
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkDetailResponse
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkResponse
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkReviewResponse
import com.example.dogcatsquare.ui.map.walking.data.WalkCreateRequestDto
import com.example.dogcatsquare.ui.map.walking.data.WalkCreateResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface WalkApiService {

    // 산책로 목록 조회
    @POST("/api/walks")
    fun getWalkList(@Body walkRequest: Coordinate): WalkResponse

    // 특정 산책로 상세 조회
    @GET("/api/walks/{walkId}")
    fun getWalkDetail(@Path("walkId") walkId: Int): Call<WalkDetailResponse>

    //산책로 후기 조회
    @GET("/api/walks/{walkId}/reviews")
    fun getWalkReview(@Path("walkId") walkId: Int): Call<WalkReviewResponse>

    @GET("/reverse-geocode")
    fun getAddress(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Call<ResponseBody>

    //산책로 후기 등록
    @Multipart
    @POST("api/walks/{walkId}/reviews")
    fun saveWalkReview(
        @Path("walkId") walkId: Long,
        @Part("reviewCreateRequestDto") reviewCreateRequestDto: RequestBody,
        @Part walkReviewImages: List<MultipartBody.Part>
    ): Call<WalkReviewResponse>

    //산책로 등록
    @Multipart
    @POST("/api/walks/create")
    suspend fun createWalk(
        @Part("walkCreateRequestDto") walkCreateRequestDto: WalkCreateRequestDto,
        @Part walkReviewImages: List<MultipartBody.Part>
    ): WalkCreateResponse
}

