package com.example.dogcatsquare.ui.community

data class LocalPost(
    val username: String,        // 닉네임
    val dogbreed: String,        // 견종
    val images: List<Int>,       // 이미지 리스트 (리소스 ID 또는 URL)
    val content: String,         // 게시글 내용
)
