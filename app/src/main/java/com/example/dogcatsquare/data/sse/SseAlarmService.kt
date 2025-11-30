package com.example.dogcatsquare.data.api

import android.icu.util.TimeUnit
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.dogcatsquare.data.model.alarm.AlarmResponse
import com.example.dogcatsquare.data.model.alarm.SSEAlarmResponse
import com.example.dogcatsquare.data.sse.SSEClient
import com.google.gson.Gson
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.EventHandler
import com.launchdarkly.eventsource.MessageEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.time.Duration

// SSEClient 인터페이스를 구현하며, EventSource를 사용하여 실제 SSE 통신을 담당합니다.
class SseAlarmService(private val okHttpClient: OkHttpClient) : SSEClient {
    // 운영 환경 BASE URL과 SSE 구독 엔드포인트
    // 백엔드에서 SseEmitter를 리턴하는 /api/notification/subscribe 엔드포인트에 연결합니다.
    private val BASE_URL = "http://3.39.188.10:8080"
    private val SUBSCRIBE_URL = "$BASE_URL/api/notification/sse/subscribe"

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var eventSource: EventSource? = null
    private val gson = Gson()

    // 알림 이벤트를 외부에 노출하기 위한 Flow
    private val _sseEvents = MutableSharedFlow<SSEAlarmResponse>(replay = 0)
    override val sseEvents: SharedFlow<SSEAlarmResponse> = _sseEvents

    // 연결 상태를 외부에 노출하기 위한 Flow
    private val _connectionStatus = MutableSharedFlow<Boolean>(replay = 1).apply {
        tryEmit(false)
    }
    override val connectionStatus: SharedFlow<Boolean> = _connectionStatus

    @RequiresApi(Build.VERSION_CODES.O)
    override fun startListening(jwtToken: String) {
        if (eventSource != null) {
            Log.w("SSEClient", "SSE already running. eventSource is not null.")
            return
        }

        val httpUrl = SUBSCRIBE_URL.toHttpUrl() ?: run {
            Log.e("SSEClient", "Invalid URL: $SUBSCRIBE_URL")
            return
        }

        val interceptingClient = okHttpClient.newBuilder()
            .addInterceptor { chain ->
                val original = chain.request()
                // SSE 연결 요청에 필요한 Authorization 헤더와 Accept 헤더를 삽입합니다.
                val requestWithHeader = original.newBuilder()
                    .header("Authorization", "Bearer $jwtToken")
                    .header("Accept", "text/event-stream")
                    .build()
                chain.proceed(requestWithHeader)
            }
            .build()

        eventSource = EventSource.Builder(object : EventHandler {
            override fun onOpen() {
                Log.d("SSEClient", "SSE Connection Opened. SseEmitter와의 통로 열림.")
                scope.launch { _connectionStatus.emit(true) }
            }

            override fun onMessage(event: String, message: MessageEvent) {
                val json = message.data
                Log.i("SSEClient", "Received Event [$event] Data: $json")

                if (json.isNullOrBlank() || !json.trimStart().startsWith('{')) {
                    Log.w("SSEClient", "Skipping non-JSON or blank message: $json")
                    return
                }

                try {
                    val alarmResponse = gson.fromJson(json, SSEAlarmResponse::class.java)
                    scope.launch { _sseEvents.emit(alarmResponse) }
                } catch (e: Exception) {
                    // 서버에서 다른 유형의 메시지를 보내거나 (예: 로그인 필요 메시지) 데이터 형식이 깨졌을 때 발생
                    Log.e("SSEClient", "Error parsing SSE message: ${e.message}. Data: $json", e)
                }
            }

            override fun onComment(comment: String) {
                // Keep-alive 메시지 등
            }

            override fun onError(t: Throwable) {
                // 401 오류는 여기서 로깅됩니다. retryDelayMs 설정에 따라 재연결을 시도합니다.
                Log.e("SSEClient", "SSE Error: ${t.message}. Attempting reconnect if retry policy allows.")
            }

            override fun onClosed() {
                Log.d("SSEClient", "SSE Connection Closed. SseEmitter와의 통로 닫힘.")
                scope.launch { _connectionStatus.emit(false) }
            }
        }, httpUrl)
            .client(interceptingClient)
            .reconnectTime(Duration.ofMillis(3000L))
            .build()

        // 3. 새로운 쓰레드에서 SSE 연결 시작
        eventSource?.start()
        Log.d("SSEClient", "SSE Listening Attempted with token")
    }

    override fun stopListening() {
        eventSource?.close()
        Log.d("SSEClient", "SSE Listening Stopped")

        scope.launch { _connectionStatus.emit(false) }
        eventSource = null
    }
}