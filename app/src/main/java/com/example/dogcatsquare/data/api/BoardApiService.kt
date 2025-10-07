package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.model.community.*
import com.example.dogcatsquare.data.model.post.PopularPostResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface BoardApiService {

    // ===== 게시판 =====
    @POST("api/board")
    fun createBoard(
        @Header("Authorization") token: String,
        @Body createBoardRequestDto: BoardRequestDto
    ): Call<BoardResponseDto>

    @GET("api/board/all")
    fun getAllBoards(@Header("Authorization") token: String): Call<BoardSearchResponseDto>

    @GET("api/myboard")
    fun getMyBoards(@Header("Authorization") token: String): Call<MyBoardResponse>

    @POST("api/myboard")
    fun addMyBoard(
        @Header("Authorization") token: String,
        @Query("boardId") boardId: Int
    ): Call<MyBoardResponse>

    @DELETE("api/myboard/{myBoardId}")
    fun deleteMyBoard(
        @Header("Authorization") token: String,
        @Path("myBoardId") boardId: Int
    ): Call<DeleteMyBoardResponse>

    @GET("api/myboard/home")
    fun getMyBoardHome(@Header("Authorization") token: String): Call<MyBoardResponse>

    @GET("api/board/search")
    fun searchBoard(
        @Header("Authorization") token: String,
        @Query("boardName") boardName: String
    ): Call<BoardSearchResponseDto>

    // ===== 게시글 =====
    @GET("api/board/{boardId}/posts")
    fun getPosts(@Path("boardId") boardId: Long): Call<PostListResponse>

    @GET("api/board/posts/popular")
    fun getPopularPost(@Header("Authorization") token: String): Call<PopularPostResponse>

    /** 게시글 생성: request는 JSON 텍스트 파트로 전송 */
    @Multipart
    @POST("api/board/post/users/{userId}")
    fun createPost(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long,
        @Part("request") request: RequestBody,
        @Part communityImages: List<@JvmSuppressWildcards MultipartBody.Part>? = null
    ): Call<ApiResponse<PostResponse>>

    /** 게시글 수정 (기존 코드 유지해도 OK) */
    @Multipart
    @PUT("api/board/post/{postId}")
    fun updatePost(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long,
        @Part("request") request: RequestBody,
        @Part communityImages: List<@JvmSuppressWildcards MultipartBody.Part>? = null
    ): Call<ApiResponse<Unit>>

    @GET("api/board/post/{postId}")
    fun getPost(
        @Header("Authorization") token: String,
        @Path("postId") postId: Int
    ): Call<ApiResponse<PostDetail>>

    /** 게시글 삭제 */
    @DELETE("api/board/post/{postId}")
    fun deletePost(
        @Header("Authorization") token: String,
        @Path("postId") postId: Int
    ): Call<ApiResponse<Unit>>
}