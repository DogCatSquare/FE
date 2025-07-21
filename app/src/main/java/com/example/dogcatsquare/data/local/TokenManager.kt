package com.example.dogcatsquare.data.local

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun saveUserInfo(id: Int, email: String, pw: String, cityId: Long) {
        prefs.edit()
            .putInt(KEY_USER_ID, id)
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, pw)
            .putLong(KEY_CITY_ID, cityId)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)
    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)
    fun getPassword(): String? = prefs.getString(KEY_PASSWORD, null)
    fun getCityId(): Long = prefs.getLong(KEY_CITY_ID, -1)

    fun isAutoLogin(): Boolean = prefs.getBoolean(KEY_AUTO_LOGIN, false)

    fun setAutoLogin(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_LOGIN, enabled).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "token"
        private const val KEY_REFRESH_TOKEN = "refreshToken"
        private const val KEY_USER_ID = "userId"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "pw"
        private const val KEY_CITY_ID = "cityId"
        private const val KEY_AUTO_LOGIN = "autoLogin"
    }
}