package com.example.dogcatsquare.data.community

import com.google.gson.annotations.SerializedName

data class MyBoardResponse(
    @SerializedName("isSuccess") val isSuccess: Boolean,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("result") val result: List<MyBoardResult>
)
data class MyBoardResult(
    @SerializedName("id") val id: Int,
    @SerializedName("boardId") val boardId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("boardName") val boardName: String
)
