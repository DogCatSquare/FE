package com.example.dogcatsquare.data.model.community

data class LocalPost(
    val id: Long,               // Long 타입의 id 추가 (기존에 문자열이었다면 변경)
    val username: String,
    val dogbreed: String,
    val title: String?,
    val content: String?,
    val video_URL: String?,     // null 가능
    val thumbnail_URL: String?, // null 가능
    val images: List<Any>       // 만약 이미지가 리소스 ID가 아니라 URL 혹은 Int라면 맞춰 수정
)
