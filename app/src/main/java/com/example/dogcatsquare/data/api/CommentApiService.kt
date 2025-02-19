package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.community.CommentListResponse
import com.example.dogcatsquare.data.community.CommentResponse
import com.example.dogcatsquare.data.community.CommonResponse
import com.example.dogcatsquare.data.community.CommentRequest
import retrofit2.Call
import retrofit2.http.*

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
