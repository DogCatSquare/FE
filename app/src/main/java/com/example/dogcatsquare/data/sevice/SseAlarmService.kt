// com.example.dogcatsquare.SseAlarmService.kt (최상위 또는 service 폴더에 생성)

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.local.TokenManager
import com.example.dogcatsquare.utils.SseManager

class SseAlarmService : Service() {

    private val CHANNEL_ID = "SseNotificationChannel"
    private val NOTIFICATION_ID = 101 // 포그라운드 서비스는 고유 ID 필요

    // SseManager를 서비스가 관리합니다.
    private lateinit var tokenManager: TokenManager // JWT 토큰을 관리하는 클래스 가정

    override fun onCreate() {
        super.onCreate()
        // 토큰 관리자를 초기화합니다. (프로젝트 구조에 맞게 수정 필요)
        tokenManager = TokenManager(applicationContext)

        // Android O(8.0) 이상에서 포그라운드 서비스 알림 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. 포그라운드 서비스 시작
        val notification = buildForegroundNotification()
        startForeground(NOTIFICATION_ID, notification)

        // 2. JWT 토큰을 가져와서 SSE 연결 시작
        val jwt = tokenManager.getAccessToken() // JWT를 안전하게 가져오는 메서드 가정
        if (!jwt.isNullOrEmpty()) {
            SseManager.start(jwt)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        SseManager.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // 바인딩이 필요 없으므로 null 반환
    }

    // --- 포그라운드 서비스 알림 관련 함수 ---

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val name = "실시간 알림 연결"
        val descriptionText = "앱 실행 중 실시간 알림을 위한 연결을 유지합니다."
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("알림 서비스 실행 중")
            .setContentText("실시간 알림 연결을 유지하고 있습니다.")
            .setSmallIcon(R.drawable.ic_notification) // 프로젝트의 알림 아이콘으로 변경
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
    }
}