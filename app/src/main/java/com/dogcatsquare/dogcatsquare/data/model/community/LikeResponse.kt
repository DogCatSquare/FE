package com.dogcatsquare.dogcatsquare.data.model.community

data class LikeResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: String
)
