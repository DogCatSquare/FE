package com.example.dogcatsquare.data.post

data class Post(
    val id: Int,
    val title: String,
    val content: String,
    val nickname: String,
    val pets: List<Pet>,
    val likeCount: Int?,
    val commentCount: Int?
)
data class Pet(
    val name: String,
    val breed: String
)
