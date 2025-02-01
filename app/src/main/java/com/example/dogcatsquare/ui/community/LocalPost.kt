package com.example.dogcatsquare.ui.community

data class LocalPost(
    val id: String,
    val username: String,
    val dogbreed: String,
    val images: List<Int>,
    val content: String,
    val title: String,         // 🔹 게시글 제목 추가
    val video_URL: String?,    // 🔹 동영상 URL 추가
    val thumbnail_URL: String? // 🔹 썸네일 이미지 URL 추가
)
