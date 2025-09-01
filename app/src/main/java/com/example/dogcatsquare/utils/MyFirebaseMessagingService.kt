package com.example.dogcatsquare.utils

import android.util.Log
import com.example.dogcatsquare.data.network.RetrofitObj
import com.google.firebase.messaging.FirebaseMessagingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // 1) 로컬 저장
        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit().putString("fcm_token", token).apply()

        // 2) 서버로 업로드 (예: 토큰 등록 API)
        // 안전하게 WorkManager/Coroutine으로 전송
        GlobalScope.launch(Dispatchers.IO) {
//            try {
//                val api = RetrofitObj.getRetrofit(applicationContext).create(PushApi::class.java)
//                // accessToken은 네 앱 로그인 토큰(예시)
//                val accessToken = getSharedPreferences("app_prefs", MODE_PRIVATE).getString("token", null)
//                if (!accessToken.isNullOrBlank()) {
//                    api.registerToken("Bearer $accessToken", TokenRegisterReq(token))
//                }
//            } catch (e: Exception) {
//                Log.e("FCM", "register token failed: ${e.message}")
//            }
        }
    }

}