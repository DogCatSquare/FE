package com.dogcatsquare.dogcatsquare.utils

import android.app.Activity
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.dogcatsquare.dogcatsquare.R

object StatusBarHelper {
    fun applySavedStatusBarColor(activity: Activity) {
        val sp = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val colorResId = sp.getInt("status_bar_color", R.color.home_yellow)
        
        activity.window.apply {
            clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = ContextCompat.getColor(activity, colorResId)
        }

        // 상태바 아이콘 색상 설정 (배경색이 모두 밝은 계열이므로 항상 어두운 아이콘으로 설정)
        val decorView = activity.window.decorView
        WindowCompat.getInsetsController(activity.window, decorView).apply {
            isAppearanceLightStatusBars = true
        }
    }

    fun saveStatusBarColor(context: Context, colorResId: Int) {
        val sp = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sp.edit().putInt("status_bar_color", colorResId).apply()
    }
}
