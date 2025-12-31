package com.example.dogcatsquare.utils

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

object SseManager {

    private const val TAG = "SseManager"
    private const val BASE_URL = "http://3.39.188.10:8080"

    private val client = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .build()

    private var eventSource: EventSource? = null

    fun start(jwt: String) {
        if (eventSource != null) return

        val request = Request.Builder()
            .url("$BASE_URL/api/notification/sse/subscribe")
            .addHeader("Authorization", "Bearer $jwt")
            .build()

        eventSource = EventSources.createFactory(client)
            .newEventSource(request, object : EventSourceListener() {

                override fun onOpen(es: EventSource, response: okhttp3.Response) {
                    Log.d(TAG, "SSE 연결 성공")
                }

                override fun onEvent(
                    es: EventSource,
                    id: String?,
                    type: String?,
                    data: String
                ) {
                    Log.d(TAG, "SSE 이벤트 수신: $data")

                    // data를 JSON으로 파싱해서 알림 객체로 바꾸고,
                    // in-app 배너나 Notification으로 띄우기
                    handleNotificationFromSse(data)
                }

                override fun onClosed(es: EventSource) {
                    Log.d(TAG, "SSE 연결 종료")
                    if (eventSource == es) eventSource = null
                }

                override fun onFailure(
                    es: EventSource,
                    t: Throwable?,
                    response: okhttp3.Response?
                ) {
                    Log.e(TAG, "SSE 에러", t)
                    if (eventSource == es) eventSource = null
                    // 필요하면 재연결 로직 추가
                }
            })
    }

    fun stop() {
        eventSource?.cancel()
        eventSource = null
    }

    private fun handleNotificationFromSse(data: String) {
        // 예시: {"id":1,"title":"새 댓글","body":"..."} 라고 온다고 가정
        // Gson / kotlinx.serialization 로 파싱해서 Notification 띄우면 됨
    }
}
