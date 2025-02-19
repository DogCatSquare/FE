package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.model.login.CheckEmailResponse
import com.example.dogcatsquare.data.model.login.CheckNicknameResponse
import com.example.dogcatsquare.data.model.login.DeleteUserResponse
import com.example.dogcatsquare.data.model.login.LoginRequest
import com.example.dogcatsquare.data.model.login.LoginResponse
import com.example.dogcatsquare.data.model.login.RefreshTokenResponse
import com.example.dogcatsquare.data.model.login.SendVerficationRequest
import com.example.dogcatsquare.data.model.login.SendVerficationResponse
import com.example.dogcatsquare.data.model.login.SignUpRequest
import com.example.dogcatsquare.data.model.login.SignUpResponse
import com.example.dogcatsquare.data.model.login.VerifyRequest
import com.example.dogcatsquare.data.model.login.VerifyResponse
import com.example.dogcatsquare.data.model.mypage.FetchUserResponse
import com.example.dogcatsquare.data.model.mypage.GetUserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
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

    @POST("api/email/send-verification")
    fun sendVerification(@Body sendVerficationRequest: SendVerficationRequest): Call<SendVerficationResponse>

    @POST("api/email/verify")
    fun verifyEmail(@Body verifyRequest: VerifyRequest): Call<VerifyResponse>

    @POST("api/users/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("api/token/refresh")
    fun refreshToken(@Header("RefreshToken") refreshToken: String): Call<RefreshTokenResponse>

    @GET("api/users/users-inquiry")
    fun getUser(@Header("Authorization") token: String): Call<GetUserResponse>

    @Multipart
    @PUT("api/users/update")
    fun fetchUser(@Header("Authorization") token: String, @Part("request") request: RequestBody, @Part profileImage: MultipartBody.Part?): Call<FetchUserResponse>

    @DELETE("api/users")
    fun deleteUser(@Header("Authorization") token: String): Call<DeleteUserResponse>
}