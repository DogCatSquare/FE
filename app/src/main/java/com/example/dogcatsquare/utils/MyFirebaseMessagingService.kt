package com.example.dogcatsquare.utils

import android.util.Log
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.model.home.RegisterFcmRequest
import com.example.dogcatsquare.data.network.RetrofitObj
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // 1) 로컬 저장
        getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit().putString("fcm_token", token).apply() // 활성화

        // 2) 서버로 업로드 (예: 토큰 등록 API)
        // 안전하게 WorkManager/Coroutine으로 전송
        GlobalScope.launch(Dispatchers.IO) { // 활성화
            try {
                // RetrofitObj를 사용하여 UserRetrofitItf 인터페이스 생성
                val api = RetrofitObj.getRetrofit(applicationContext).create(UserRetrofitItf::class.java)

                // accessToken과 userId (예시: 로그인 시 저장된 정보) 가져오기
                val sharedPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
                val accessToken = sharedPrefs.getString("token", null)
                // 💡 userId는 앱의 로그인/회원가입 시점에 SharedPreferences에 저장했다고 가정합니다.
                val userId = sharedPrefs.getInt("userId", -1)

                if (!accessToken.isNullOrBlank() && userId != -1) {
                    // ✅ fcmToken 함수 호출 완성
                    val bearerToken = "Bearer $accessToken"
                    val request = RegisterFcmRequest(userId, token)

                    val response = api.fcmToken(bearerToken, request).execute() // execute()로 동기 호출 (코루틴 내부이므로 안전)

                    if (response.isSuccessful) {
                        Log.d("FCM", "Token registered successfully for userId: $userId")
                    } else {
                        Log.e("FCM", "Token registration failed: ${response.code()}")
                    }
                } else {
                    Log.e("FCM", "Cannot register token: Missing accessToken or userId.")
                }
            } catch (e: Exception) {
                Log.e("FCM", "register token failed: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
    }
}