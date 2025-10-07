package com.example.dogcatsquare.data.model.community

import com.google.gson.annotations.SerializedName

data class UpdatePostRequest(
    @SerializedName("boardType") val boardType: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("videoUrl")  val videoUrlCamel: String? = null,  // null=유지, ""=삭제, "http..."=수정
    @SerializedName("video_URL") val videoUrlSnake: String? = null,
    @SerializedName("images") val images: List<String>? = null,
    @SerializedName("removeImageUrls") val removeImageUrls: List<String>? = null
)