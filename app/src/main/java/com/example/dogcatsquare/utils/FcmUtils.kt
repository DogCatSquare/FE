package com.example.dogcatsquare.utils

import android.content.Context
import android.util.Log
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.model.home.RegisterFcmRequest
import com.example.dogcatsquare.data.network.RetrofitObj
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FcmUtils {
    // 1. 최신 토큰을 가져와서 서버에 보내는 함수
    fun updateFcmToken(context: Context) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                Log.d("FCM", "최신 토큰 획득: $fcmToken")
                sendRegistrationToServer(context, fcmToken)
            }
        }
    }

    // 2. 실제 서버 API를 호출하는 함수
    fun sendRegistrationToServer(context: Context, token: String) {
        val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val accessToken = sharedPrefs.getString("token", null)
        val userId = sharedPrefs.getInt("userId", -1)

        if (!accessToken.isNullOrBlank() && userId != -1) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val api = RetrofitObj.getRetrofit(context).create(UserRetrofitItf::class.java)
                    val bearerToken = "Bearer $accessToken"
                    val request = RegisterFcmRequest(userId, token)

                    val response = api.fcmToken(bearerToken, request).execute()
                    if (response.isSuccessful) {
                        Log.d("FCM", "서버 등록 성공: $userId")
                    } else {
                        Log.e("FCM", "서버 등록 실패: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Log.e("FCM", "네트워크 에러: ${e.message}")
                }
            }
        }
    }
}