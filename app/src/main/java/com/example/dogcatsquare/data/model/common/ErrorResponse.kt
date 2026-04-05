package com.example.dogcatsquare.data.model.common

data class ErrorResponse(
    val isSuccess: Boolean? = null,
    val code: String? = null,
    val message: String? = null,
    val result: Any? = null
)