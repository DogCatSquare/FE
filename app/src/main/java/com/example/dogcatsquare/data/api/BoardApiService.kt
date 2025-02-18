package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.community.ApiResponse
import com.example.dogcatsquare.data.community.BoardRequestDto
import com.example.dogcatsquare.data.community.BoardResponseDto
import com.example.dogcatsquare.data.community.BoardSearchResponseDto
import com.example.dogcatsquare.data.community.PostDetailResponse
import com.example.dogcatsquare.data.community.PostListResponse
import com.example.dogcatsquare.data.community.PostResponse
import com.example.dogcatsquare.data.model.post.PopularPostResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface BoardApiService {
    // 게시판 생성 API
    @POST("/api/board")
    fun createBoard(
        @Header("Authorization") token: String,
        @Body boardRequest: BoardRequestDto
    ): Call<BoardResponseDto>

    // 게시글 등록 API
    @Multipart
    @POST("api/board/post/users/{userId}")
    fun createPost(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long,
        @Part("request") requestBody: RequestBody,
        @Part images: List<MultipartBody.Part>?
    ): Call<ApiResponse>

    // 게시글 수정 API
    @Multipart
    @PUT("/api/board/post/{postId}")
    fun updatePost(
        @Path("postId") postId: Long,
        @Part("request") request: RequestBody,
        @Part communityImages: List<MultipartBody.Part>?
    ): Call<ApiResponse>

    // 특정 게시글 조회 API
    @GET("api/board/{boardType}/post/{postId}")
    fun getPost(
        @Path("boardType") boardType: String,
        @Path("postId") postId: Long
    ): Call<PostDetailResponse>

    // 특정게시판에 있는 게시글들 조회
    @GET("api/board/{boardId}/posts")
    fun getPosts(
        @Path("boardId") boardId: Long
    ): Call<PostListResponse>

    // 모든 게시판 조회 API
    @GET("/api/board/all")
    fun getAllBoards(): Call<BoardSearchResponseDto>

    // 게시판 검색 API
    @GET("/api/board/search")
    fun searchBoard(@Query("boardName") boardName: String): Call<BoardSearchResponseDto>

    @GET("api/board/posts/popular")
    fun getPopularPost(@Header("Authorization") token: String): Call<PopularPostResponse>

}