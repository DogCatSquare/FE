package com.example.dogcatsquare.data.sse

import com.example.dogcatsquare.data.model.alarm.SSEAlarmResponse
import kotlinx.coroutines.flow.SharedFlow

interface SSEClient {
    // SSE 연결을 시작합니다.
    fun startListening(jwtToken: String)

    // SSE 연결을 종료합니다.
    fun stopListening()

    // 수신된 알림 이벤트를 외부에 노출하는 Flow
    val sseEvents: SharedFlow<SSEAlarmResponse>

    // 연결 상태를 외부에 노출하는 Flow
    val connectionStatus: SharedFlow<Boolean>
}