package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.model.community.*
import com.example.dogcatsquare.data.model.post.PopularPostResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface BoardApiService {

    // ==================== 게시판 ====================

    /** 게시판 생성 */
    @POST("api/board")
    fun createBoard(
        @Header("Authorization") token: String,
        @Body createBoardRequestDto: BoardRequestDto
    ): Call<BoardResponseDto>

    /** 모든 게시판 조회 */
    @GET("api/board/all")
    fun getAllBoards(
        @Header("Authorization") token: String
    ): Call<BoardSearchResponseDto>

    /** 내 관심 게시판 목록 */
    @GET("api/myboard")
    fun getMyBoards(
        @Header("Authorization") token: String
    ): Call<MyBoardResponse>

    /** 관심 게시판 추가 */
    @POST("api/myboard")
    fun addMyBoard(
        @Header("Authorization") token: String,
        @Query("boardId") boardId: Int
    ): Call<MyBoardResponse>

    /** 관심 게시판 삭제 */
    @DELETE("api/myboard/{myBoardId}")
    fun deleteMyBoard(
        @Header("Authorization") token: String,
        @Path("myBoardId") boardId: Int
    ): Call<DeleteMyBoardResponse>

    /** 내 관심 게시판 홈 */
    @GET("api/myboard/home")
    fun getMyBoardHome(
        @Header("Authorization") token: String
    ): Call<MyBoardResponse>

    /** 게시판 검색 (토큰 요구 버전만 유지) */
    @GET("api/board/search")
    fun searchBoard(
        @Header("Authorization") token: String,
        @Query("boardName") boardName: String
    ): Call<BoardSearchResponseDto>

    // ==================== 게시글 ====================

    /** 특정 게시판 내 게시글 목록 */
    @GET("api/board/{boardId}/posts")
    fun getPosts(
        @Path("boardId") boardId: Long
    ): Call<PostListResponse>

    /** 인기 게시글 */
    @GET("api/board/posts/popular")
    fun getPopularPost(
        @Header("Authorization") token: String
    ): Call<PopularPostResponse>

    /** 게시글 생성 (멀티파트) */
    @Multipart
    @POST("api/board/post/users/{userId}")
    fun createPost(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long,
        @Part("request") requestBody: RequestBody, // application/json
        @Part communityImages: List<@JvmSuppressWildcards MultipartBody.Part>? // 파트명 스웨거 기준
    ): Call<ApiResponse<PostResponse>>

    /** 게시글 수정 (멀티파트) */
    @Multipart
    @PUT("api/board/post/{postId}")
    fun updatePost(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long,
        @Part("request") requestJson: RequestBody, // application/json
        @Part communityImages: List<@JvmSuppressWildcards MultipartBody.Part> = emptyList()
    ): Call<ApiResponse<PostResponse>>

    /** 게시글 단건 조회 */
    @GET("api/board/post/{postId}")
    fun getPost(
        @Header("Authorization") token: String,
        @Path("postId") postId: Int
    ): Call<ApiResponse<PostDetail>>
}