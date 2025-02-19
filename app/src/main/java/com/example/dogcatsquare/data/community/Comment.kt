package com.example.dogcatsquare.data.community

import com.google.gson.annotations.SerializedName

data class CommentResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: Comment
)

data class Comment(
    val id: Int = 0,
    val content: String,
    val name: String,
    @SerializedName("animal_type")
    val animalType: String = "",
    @SerializedName("profileImage_URL")
    val profileImageUrl: String = "",
    @SerializedName("created_at")
    val timestamp: String,
    val replies: List<Reply>,
    val parentId: String? = ""  // 기본값은 빈 문자열 또는 null로 설정
)

data class CommentRequest(
    val content: String,
    val parentId: String?  // 일반 댓글인 경우 null도 가능하게 처리
)

data class Reply(
    val id: Int,
    val content: String,
    val name: String,
    val dogBreed: String,       // 예시로 추가
    val profileImageUrl: String,
    val timestamp: String
)

data class CommonResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: Any? // 빈 객체로 반환됨
)

data class CommentListResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: List<Comment>
)
