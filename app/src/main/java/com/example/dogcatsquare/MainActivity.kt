package com.example.dogcatsquare

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.dogcatsquare.databinding.ActivityMainBinding
import com.example.dogcatsquare.ui.community.CommunityFragment
import com.example.dogcatsquare.ui.home.HomeFragment
import com.example.dogcatsquare.ui.map.location.MapFragment
import com.example.dogcatsquare.ui.mypage.MypageFragment
import com.example.dogcatsquare.ui.wish.WishFragment
import android.content.Context
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.example.dogcatsquare.data.api.SseAlarmService
import com.example.dogcatsquare.data.sse.SSEClient
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val locationViewModel: LocationViewModel by viewModels()

    val sseClient: SSEClient by lazy {
        SseAlarmService(OkHttpClient.Builder().build())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상단바 색깔
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        initBottomNavigation()

        setupSseConnection()
    }

    private fun setupSseConnection() {
        val sp = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sp.getString("token", null)
        if (!token.isNullOrBlank()) {
            sseClient.startListening(token)
            lifecycleScope.launch {
                sseClient.sseEvents.collectLatest { sseAlarm ->
                    showSseNotification(sseAlarm.type ?: "새로운 알림", sseAlarm.content)
                }
            }
        }
    }

    private fun showSseNotification(title: String, messageBody: String) {
        val channelId = "sse_channel"
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "실시간 앱내 알림",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        sseClient.stopListening()
    }

    // 바텀네비게이션 아이콘 선택 시 각 프래그먼트로 이동하는 함수
    private fun initBottomNavigation(){
        // 기본은 홈프래그먼트
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, HomeFragment())
            .commitAllowingStateLoss()

        binding.mainBnv.itemIconTintList = null

        binding.mainBnv.setOnItemSelectedListener{ item ->
            when (item.itemId) {

                R.id.homeFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, HomeFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.mapFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, MapFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
                R.id.communityFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, CommunityFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
                R.id.wishFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, WishFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
                R.id.mypageFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, MypageFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }
}