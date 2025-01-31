package com.example.dogcatsquare.data.community

import android.content.Context
import android.content.SharedPreferences

object SharedPrefManager {

    private const val PREFS_NAME = "UserPrefs"
    private const val KEY_USER_ID = "USER_ID"
    private const val KEY_JWT_TOKEN = "JWT_TOKEN"

    // userId 저장
    fun saveUserId(context: Context, userId: Int) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(KEY_USER_ID, userId)
        editor.apply()
    }

    // userId 불러오기
    fun getUserId(context: Context): Int {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(KEY_USER_ID, 0) // 기본값 0
    }

    // JWT Token 저장
    fun saveJwtToken(context: Context, token: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_JWT_TOKEN, token)
        editor.apply()
    }

    // JWT Token 불러오기
    fun getJwtToken(context: Context): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_JWT_TOKEN, null)
    }

    // 로그아웃 시 저장된 데이터 삭제
    fun clearUserData(context: Context) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear() // 모든 데이터 삭제
        editor.apply()
    }
}
