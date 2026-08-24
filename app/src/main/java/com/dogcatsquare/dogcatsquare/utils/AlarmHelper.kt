package com.dogcatsquare.dogcatsquare.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dogcatsquare.dogcatsquare.data.model.home.DDay
import java.text.SimpleDateFormat
import java.util.*

object AlarmHelper {
    @SuppressLint("ScheduleExactAlarm")
    fun setDdayAlarm(context: Context, dDay: DDay) {
        if (!dDay.isAlarm) {
            Log.d("AlarmHelper", "⏰ isAlarm이 false이므로 알람을 설정하지 않음")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DdayAlarmReceiver::class.java).apply {
            putExtra("DDAY_TITLE", dDay.title)
            putExtra("DDAY_MESSAGE", "오늘은 ${dDay.title} 디데이입니다!")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, dDay.id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // 🔹 날짜 변환 (현재 입력 값이 "yyyy. MM. dd" 형식일 경우)
            val inputFormat = SimpleDateFormat("yyyy. MM. dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val date = inputFormat.parse(dDay.day)
            val formattedDate = outputFormat.format(date!!)

            val calendar = Calendar.getInstance().apply {
                time = outputFormat.parse(formattedDate) ?: return
                set(Calendar.HOUR_OF_DAY, 9)  // 오전 9시
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            // 과거 시간이면 알람 설정 안 함
            if (calendar.timeInMillis < System.currentTimeMillis()) {
                Log.d("AlarmHelper", "⏰ 디데이 날짜가 과거이므로 알람 설정 안 함")
                return
            }

            Log.d("AlarmHelper", "✅ 알람 설정됨: ${dDay.title} (${formattedDate}) 오전 9시")
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } catch (e: Exception) {
            Log.e("AlarmHelper", "❌ 날짜 변환 실패: ${e.message}")
        }
    }

    // ✅ 새로운 메서드: D-Day 알람 취소
    fun cancelDdayAlarm(context: Context, dDayId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DdayAlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context, dDayId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d("AlarmHelper", "❌ 알람 취소됨: D-Day ID $dDayId")
        alarmManager.cancel(pendingIntent)
    }
}