package com.dogcatsquare.dogcatsquare.data.model.community

data class BoardSearchResponseDto(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<com.dogcatsquare.dogcatsquare.data.model.community.BoardData>
)