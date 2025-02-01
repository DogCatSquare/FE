package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.login.CheckEmailResponse
import com.example.dogcatsquare.data.login.CheckNicknameResponse
import com.example.dogcatsquare.data.login.LoginRequest
import com.example.dogcatsquare.data.login.LoginResponse
import com.example.dogcatsquare.data.login.SignUpRequest
import com.example.dogcatsquare.data.login.SignUpResponse
import com.example.dogcatsquare.data.mypage.FetchUserResponse
import com.example.dogcatsquare.data.mypage.GetUserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface UserRetrofitItf {
    @Multipart
    @POST("api/users/register")
    fun signup(@Part("request") requestBody: RequestBody, @Part profileImage: MultipartBody.Part?, @Part petImage: MultipartBody.Part?): Call<SignUpResponse>

    @GET("api/users/check-nickname")
    fun checkNickname(@Query("nickname") nickname: String): Call<CheckNicknameResponse>

    @GET("api/users/check-email")
    fun checkEmail(@Query("email") email: String): Call<CheckEmailResponse>

//    @POST
//    fun verifyEmail(): Call<>
//
//    @POST
//    fun sendVerification(): Call<>

    @POST("api/users/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET("api/users/users-inquiry")
    fun getUser(@Header("Authorization") token: String): Call<GetUserResponse>

    @Multipart
    @PUT("api/users/update")
    fun fetchUser(@Header("Authorization") token: String, @Part("request") requestBody: RequestBody, @Part profileImage: MultipartBody.Part?): Call<FetchUserResponse>
}