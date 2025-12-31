package com.example.dogcatsquare.data.model.alarm

import com.google.gson.annotations.SerializedName

data class AlarmResponse(
    @SerializedName("timeout") val timeout: Int
)

data class ReadNotificationRequest(
    @SerializedName("ids") val ids: List<Long>?
)

data class SSEAlarmResponse(
    val id: Long,
    val userId: Long,
    val type: String,      // 알림 유형 (LIKE, DDAY 등)
    val content: String,   // 사용자에게 보여줄 메시지 내용 (content 사용)
    val createdAt: String, // 알림 생성 시간
    val read: Boolean
)