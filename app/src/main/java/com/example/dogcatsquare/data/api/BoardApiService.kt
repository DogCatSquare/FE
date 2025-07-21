package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.model.community.ApiResponse
import com.example.dogcatsquare.data.model.community.BoardRequestDto
import com.example.dogcatsquare.data.model.community.BoardResponseDto
import com.example.dogcatsquare.data.model.community.BoardSearchResponseDto
import com.example.dogcatsquare.data.model.community.CommentResponse
import com.example.dogcatsquare.data.model.community.CommonResponse
import com.example.dogcatsquare.data.model.community.DeleteMyBoardResponse
import com.example.dogcatsquare.data.model.community.GetMyBoardHomeResponse
import com.example.dogcatsquare.data.model.community.MyBoardResponse
import com.example.dogcatsquare.data.model.community.PostDetailResponse
import com.example.dogcatsquare.data.model.community.PostListResponse
import com.example.dogcatsquare.data.model.community.PostResponse
import com.example.dogcatsquare.data.model.post.PopularPostResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface BoardApiService {
    // 게시판 생성 API
    @POST("api/board")
    fun createBoard(
        @Header("Authorization") token: String,
        @Body createBoardRequestDto: com.example.dogcatsquare.data.model.community.BoardRequestDto
    ): Call<com.example.dogcatsquare.data.model.community.BoardResponseDto>

    // 게시글 등록 API
    @Multipart
    @POST("api/board/post/users/{userId}")
    fun createPost(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long,
        @Part("request") requestBody: RequestBody,
        @Part images: List<MultipartBody.Part>?
    ): Call<com.example.dogcatsquare.data.model.community.ApiResponse>

    // 게시글 수정 API
    @Multipart
    @PUT("api/board/post/{postId}")
    fun updatePost(
        @Path("postId") postId: Long,
        @Part("request") request: RequestBody,
        @Part communityImages: List<MultipartBody.Part>?
    ): Call<com.example.dogcatsquare.data.model.community.ApiResponse>

    // 특정 게시글 조회 API
    @GET("api/board/post/{postId}")
    fun getPost(@Header("Authorization") token: String, @Path("postId") postId: Int): Call<com.example.dogcatsquare.data.model.community.PostDetailResponse>

    // 특정게시판에 있는 게시글들 조회
    @GET("api/board/{boardId}/posts")
    fun getPosts(
        @Path("boardId") boardId: Long
    ): Call<com.example.dogcatsquare.data.model.community.PostListResponse>

    // 모든 게시판 조회 API
    @GET("api/board/all")
    fun getAllBoards(@Header("Authorization") token: String): Call<com.example.dogcatsquare.data.model.community.BoardSearchResponseDto>

    @GET("api/myboard")
    fun getMyBoards(@Header("Authorization") token: String): Call<com.example.dogcatsquare.data.model.community.MyBoardResponse>

    @POST("api/myboard")
    fun addMyBoard(@Header("Authorization") token: String, @Query("boardId") boardId: Int): Call<com.example.dogcatsquare.data.model.community.MyBoardResponse>

    @DELETE("api/myboard/{myBoardId}")
    fun deleteMyBoard(@Header("Authorization") token: String, @Path("myBoardId") boardId: Int): Call<com.example.dogcatsquare.data.model.community.DeleteMyBoardResponse>

    @GET("api/myboard/home")
    fun getMyBoardHome(@Header("Authorization") token: String): Call<com.example.dogcatsquare.data.model.community.MyBoardResponse>

    @GET("api/board/search")
    fun searchBoard(@Header("Authorization") token: String, @Query("boardName") boardName: String): Call<com.example.dogcatsquare.data.model.community.BoardSearchResponseDto>

    @GET("/api/board/search")
    fun searchBoard(@Query("boardName") boardName: String): Call<com.example.dogcatsquare.data.model.community.BoardSearchResponseDto>

    @GET("api/board/posts/popular")
    fun getPopularPost(@Header("Authorization") token: String): Call<PopularPostResponse>

}