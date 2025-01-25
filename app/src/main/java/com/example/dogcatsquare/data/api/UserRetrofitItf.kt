package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.login.CheckEmailResponse
import com.example.dogcatsquare.data.login.CheckNicknameResponse
import com.example.dogcatsquare.data.login.LoginRequest
import com.example.dogcatsquare.data.login.LoginResponse
import com.example.dogcatsquare.data.login.SignUpRequest
import com.example.dogcatsquare.data.login.SignUpResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface UserRetrofitItf {
    @POST("/api/users/register")
    fun signup(@Body signupRequest: SignUpRequest): Call<SignUpResponse>

    @GET("api/users/check-nickname")
    fun checkNickname(@Query("nickname") nickname: String): Call<CheckNicknameResponse>

    @GET("api/users/check-email")
    fun checkEmail(@Query("email") email: String): Call<CheckEmailResponse>

//    @POST
//    fun verifyEmail(): Call<>
//
//    @POST
//    fun sendVerification(): Call<>

    @POST("/api/users/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>
}