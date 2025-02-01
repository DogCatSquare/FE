package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.pet.AddPetResponse
import com.example.dogcatsquare.data.pet.DeletePetResponse
import com.example.dogcatsquare.data.pet.FetchPetResponse
import com.example.dogcatsquare.data.pet.GetAllPetResponse
import com.example.dogcatsquare.data.pet.GetPetInfoResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface PetRetrofitItf {
    @GET("api/pets/{petId}")
    fun getPetInfo(@Path("petId") petId: Int): Call<GetPetInfoResponse>

    @Multipart
    @PUT("api/pets/{petId}")
    fun fetchPet(@Path("petId") petId: Int, @Part("request") requestBody: RequestBody, @Part petImage: MultipartBody.Part?): Call<FetchPetResponse>

    @DELETE("api/pets/{petId}")
    fun deletePet(@Path("petId") petId: Int): Call<DeletePetResponse>

    @GET("api/pets")
    fun getAllPet(@Header("Authorization") token: String): Call<GetAllPetResponse>

    @Multipart
    @POST("api/pets")
    fun addPet(@Header("Authorization") token: String, @Part("request") requestBody: RequestBody, @Part petImage: MultipartBody.Part?): Call<AddPetResponse>
}