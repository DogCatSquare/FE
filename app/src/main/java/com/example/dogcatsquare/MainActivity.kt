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
import androidx.lifecycle.MutableLiveData
import com.example.dogcatsquare.data.model.home.Alarm
import com.example.dogcatsquare.data.api.SseAlarmService
import com.example.dogcatsquare.data.sse.SSEClient
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val locationViewModel: LocationViewModel by viewModels()

    val unreadCount = MutableLiveData<Int>(0)
    val alarmList = MutableLiveData<List<Alarm>>(emptyList())
    private var isInitialConnection = false

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
        fetchUnreadCount()

        handleNotificationIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        fetchUnreadCount()
    }

    private fun fetchUnreadCount() {
        val sp = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sp.getString("token", null)
        if (!token.isNullOrBlank()) {
            val alarmApiService = com.example.dogcatsquare.data.network.RetrofitObj.getRetrofit(this)
                .create(com.example.dogcatsquare.data.api.AlarmApiService::class.java)
            lifecycleScope.launch {
                try {
                    val response = alarmApiService.getUnreadCount()
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.isSuccess == true) {
                            unreadCount.value = body.result ?: 0
                            android.util.Log.d("MainActivity", "미읽음 알림 수: ${unreadCount.value}")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "미읽음 알림 수 조회 실패: ${e.message}")
                }
            }
        }
    }

    private fun setupSseConnection() {
        val sp = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sp.getString("token", null)
        if (!token.isNullOrBlank()) {
            isInitialConnection = true
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                isInitialConnection = false
            }, 2000)

            sseClient.startListening(token)
            lifecycleScope.launch {
                sseClient.sseEvents.collectLatest { sseAlarm ->
                    val formattedDate = try {
                        com.example.dogcatsquare.util.DateFmt.format(sseAlarm.createdAt).replace(".", "-")
                    } catch (e: Exception) {
                        sseAlarm.createdAt
                    }
                    val cleanedType = sseAlarm.type?.replace(Regex("\\s*\\(postId:\\s*[^)]+\\)", RegexOption.IGNORE_CASE), "")
                    val newAlarm = Alarm(
                        id = sseAlarm.id,
                        name = cleanedType ?: "새로운 알림",
                        content = sseAlarm.content,
                        date = formattedDate,
                        type = cleanedType,
                        targetId = sseAlarm.targetId
                    )

                    val currentList = alarmList.value.orEmpty().toMutableList()
                    if (currentList.none { it.id == newAlarm.id }) {
                        currentList.add(newAlarm)
                        currentList.sortByDescending { it.id }
                        alarmList.value = currentList

                        if (!isInitialConnection) {
                            unreadCount.value = (unreadCount.value ?: 0) + 1
                            showSseNotification(cleanedType ?: "새로운 알림", sseAlarm.content, cleanedType, sseAlarm.targetId)
                        }
                    }
                }
            }
        }
    }

    private fun showSseNotification(title: String, messageBody: String, type: String?, targetId: Long?) {
        val channelId = "sse_channel"
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("notification_type", type)
            putExtra("notification_target_id", targetId)
        }
        val requestCode = System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getActivity(
            this, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent == null) return
        val type = intent.getStringExtra("notification_type")?.uppercase()
        val targetId = intent.getLongExtra("notification_target_id", -1L).takeIf { it != -1L }

        if (type != null) {
            when (type) {
                "NOTICE" -> {
                    if (targetId != null) {
                        val fragment = com.example.dogcatsquare.ui.mypage.AnnouncementDetailFragment.newInstance(targetId)
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.main_frm, fragment)
                            .addToBackStack(null)
                            .commitAllowingStateLoss()
                    }
                }
                "COMMENT", "LIKE" -> {
                    if (targetId != null) {
                        val detailIntent = Intent(this, com.example.dogcatsquare.ui.community.PostDetailActivity::class.java).apply {
                            putExtra("postId", targetId.toInt())
                        }
                        startActivity(detailIntent)
                    }
                }
                "DDAY", "TEST" -> {
                    // No navigation
                }
            }
        }
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