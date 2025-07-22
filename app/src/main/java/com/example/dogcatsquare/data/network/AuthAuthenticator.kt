package com.example.dogcatsquare.data.network

import android.util.Log
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthAuthenticator(private val tokenManager: TokenManager) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = runBlocking { tokenManager.getRefreshToken() }

        if (refreshToken.isNullOrBlank()) {
            Log.d("AuthAuthenticator", "RefreshToken 없음 → 로그인 필요")
            return null
        }

        // 새 Retrofit 인스턴스로 Refresh API 요청
        val refreshService = Retrofit.Builder()
            .baseUrl("http://3.39.188.10:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserRetrofitItf::class.java)

        return try {
            val refreshResponse = refreshService
                .refreshToken(refreshToken)
                .execute()

            if (refreshResponse.isSuccessful && refreshResponse.body()?.isSuccess == true) {
                val newAccessToken = refreshResponse.body()!!.result.accessToken
                val newRefreshToken = refreshResponse.body()!!.result.refreshToken

                runBlocking {
                    tokenManager.saveTokens(newAccessToken, newRefreshToken)
                }

                // 이전 요청에 새 토큰을 붙여서 재요청
                response.request.newBuilder()
                    .removeHeader("Authorization")
                    .addHeader("Authorization", "Bearer $newAccessToken")
                    .build()
            } else {
                Log.e("AuthAuthenticator", "Refresh 실패 → 로그인 필요")
                Log.e("AuthAuthenticator", "🔥 Refresh 실패 → ${refreshResponse.code()} / ${refreshResponse.body()?.message}")
                null
            }
        } catch (e: Exception) {
            Log.e("AuthAuthenticator", "토큰 갱신 중 오류", e)
            null
        }
    }
}