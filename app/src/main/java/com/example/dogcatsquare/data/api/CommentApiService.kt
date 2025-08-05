package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.model.community.CommentListResponse
import com.example.dogcatsquare.data.model.community.CommentResponse
import com.example.dogcatsquare.data.model.community.CommonResponse
import com.example.dogcatsquare.data.model.community.CommentRequest
import retrofit2.Call
import retrofit2.http.*

interface CommentApiService {
    @GET("api/comments/posts/{postId}")
    fun getComments(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long
    ): Call<com.example.dogcatsquare.data.model.community.CommentListResponse>

    @POST("api/comments/posts/{postId}/users/{userId}")
    fun createComment(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long,
        @Path("userId") userId: Long,
        @Body request: com.example.dogcatsquare.data.model.community.CommentRequest
    ): Call<com.example.dogcatsquare.data.model.community.CommentResponse>

    @DELETE("api/comments/posts/{postId}/{commentId}")
    fun deleteComment(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long,
        @Path("commentId") commentId: Long,
        @Path("userId") userId: Int
    ): Call<com.example.dogcatsquare.data.model.community.CommonResponse>
}
