package com.example.dogcatsquare.utils

import android.content.Context
import android.util.Log
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.model.home.RegisterFcmRequest
import com.example.dogcatsquare.data.network.RetrofitObj
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // 1) 로컬 저장
        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit().putString("fcm_token", token).apply() // 활성화

        // 공통 유틸 호출
        FcmUtils.sendRegistrationToServer(applicationContext, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: message.data["title"] ?: "새로운 알림"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val type = message.data["type"]
        val targetIdString = message.data["targetId"]
        val targetId = targetIdString?.toLongOrNull()

        sendNotification(title, body, type, targetId)
    }

    private fun sendNotification(title: String, messageBody: String, type: String?, targetId: Long?) {
        val channelId = "default_fcm_channel"
        val intent = android.content.Intent(this, com.example.dogcatsquare.SplashActivity::class.java).apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("notification_type", type)
            putExtra("notification_target_id", targetId)
        }
        val requestCode = System.currentTimeMillis().toInt()
        val pendingIntent = android.app.PendingIntent.getActivity(
            this, requestCode, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(com.example.dogcatsquare.R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "기본 푸시 알림",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}