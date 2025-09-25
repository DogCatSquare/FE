package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.model.community.ApiResponse
import com.example.dogcatsquare.data.model.community.Comment
import com.example.dogcatsquare.data.model.community.CommentRequest
import retrofit2.Call
import retrofit2.http.*

interface CommentApiService {

    @GET("api/comments/posts/{postId}")
    fun getComments(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long
    ): Call<ApiResponse<List<Comment>>>

    @POST("api/comments/posts/{postId}/users/{userId}")
    fun createComment(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long,
        @Path("userId") userId: Long,
        @Body request: CommentRequest
    ): Call<ApiResponse<Comment>>

    @DELETE("api/comments/posts/{postId}/{commentId}/users/{userId}")
    fun deleteComment(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long,
        @Path("commentId") commentId: Long,
        @Path("userId") userId: Long
    ): Call<ApiResponse<Unit>>
}