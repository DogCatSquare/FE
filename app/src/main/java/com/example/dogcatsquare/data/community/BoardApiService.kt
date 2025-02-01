package com.example.dogcatsquare.api

import com.example.dogcatsquare.data.community.ApiResponse
import com.example.dogcatsquare.data.community.BoardRequestDto
import com.example.dogcatsquare.data.community.BoardResponseDto
import com.example.dogcatsquare.data.community.PostRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface BoardApiService {
    // 게시판 생성 API
    @POST("/api/board")
    fun createBoard(
        @Header("Authorization") token: String,
        @Body requestDto: BoardRequestDto
    ): Call<BoardResponseDto>

    // 게시글 등록 API
    @Multipart
    @POST("api/board/post/users/{userId}")
    fun createPost(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int,
        @Part("request") requestBody: RequestBody,
        @Part images: List<MultipartBody.Part>?
    ): Call<ApiResponse>

    // 게시글 수정 API 추가
    @Multipart
    @PUT("/api/board/post/{postId}")
    fun updatePost(
        @Path("postId") postId: Long,
        @Part("request") request: RequestBody,
        @Part communityImages: List<MultipartBody.Part>?
    ): Call<ApiResponse>
}