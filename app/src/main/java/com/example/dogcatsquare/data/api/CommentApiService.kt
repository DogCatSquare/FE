package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.model.community.CommentListResponse
import com.example.dogcatsquare.data.model.community.CommentRequest
import com.example.dogcatsquare.data.model.community.CommentResponse
import com.example.dogcatsquare.data.model.community.CommonResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface CommentApiService {

    @GET("api/comments/posts/{postId}")
    fun getComments(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long
    ): Call<CommentListResponse>

    @POST("api/comments/posts/{postId}/users/{userId}")
    fun createComment(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long,
        @Path("userId") userId: Long,
        @Body request: CommentRequest
    ): Call<CommentResponse>

    @DELETE("api/comments/posts/{postId}/{commentId}")
    fun deleteComment(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long,
        @Path("commentId") commentId: Long
    ): Call<CommonResponse>
}