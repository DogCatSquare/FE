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
    }
}