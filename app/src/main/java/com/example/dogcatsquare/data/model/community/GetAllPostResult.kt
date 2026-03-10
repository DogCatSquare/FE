package com.example.dogcatsquare.data.model.community

data class GetAllPostResult(
    val id: Int,
    val board: String,
    val username: String,
    val animal_type: String,
    val title: String,
    val content: String,
    val videoURL: String?,
    val thumbnailURL: String?,
    val profileImageURL: String?,
    val images: List<String>,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: String
)