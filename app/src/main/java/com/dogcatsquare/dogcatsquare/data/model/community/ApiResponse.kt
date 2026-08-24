package com.dogcatsquare.dogcatsquare.data.model.community

data class ApiResponse<T>(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: T?
)