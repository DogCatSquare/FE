package com.example.dogcatsquare.data.model.common

data class BaseResponse<T>(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: T?
)