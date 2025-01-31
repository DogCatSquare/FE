package com.example.dogcatsquare

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.time.LocalDateTime
import android.Manifest.permission
import android.os.Build
import androidx.annotation.RequiresApi

class LocationService : Service() {

    // Binder 객체로, 서비스와 클라이언트 간의 연결을 위한 클래스
    private val binder = LocalBinder()
    private var locationInterface: LocationUpdateInterface? = null

    // Binder를 반환하여 서비스와 클라이언트 간의 통신을 가능하게 함
    inner class LocalBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    // 위치 정보 콜백, 위치가 업데이트 될 때마다 호출
    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            // 위치 정보가 있으면 처리
            if (locationResult.lastLocation != null) {
                val latitude = locationResult.lastLocation!!.latitude
                val longitude = locationResult.lastLocation!!.longitude
                Log.v("LOCATION_UPDATE", "$latitude, $longitude")
                locationInterface?.sendLocation(latitude, longitude)  // 위치 전달
            } else {
                Log.v("LOCATION_UPDATE", "No location data available")
            }
        }
    }


    // 서비스가 바인딩될 때 호출
    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    // 위치 업데이트 인터페이스 설정
    fun setLocationUpdateInterface(locationInterface: LocationUpdateInterface) {
        this.locationInterface = locationInterface
        Log.d("LocationService", "setLocationUpdateInterface()")
    }

    // 위치 서비스 시작
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ForegroundServiceType")
    private fun startLocationService() {
        // 알림 채널 생성
        val channelId = "location_notification_channel"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val resultIntent = Intent()
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            resultIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // 알림 빌더 설정
        val builder = NotificationCompat.Builder(applicationContext, channelId)
        builder.apply {
            setSmallIcon(R.mipmap.ic_launcher)
            setContentTitle("Location Service")
            setDefaults(NotificationCompat.DEFAULT_ALL)
            setContentText(LocalDateTime.now().toString())
            setContentIntent(pendingIntent)
            setAutoCancel(false)
            priority = NotificationCompat.PRIORITY_MAX
        }

        // 알림 채널이 없으면 새로 생성
        if (notificationManager.getNotificationChannel(channelId) == null) {
            val notificationChannel = NotificationChannel(
                channelId,
                "Location Service",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = "This channel is used by location service"
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // 위치 요청 설정 (1분 간격)
        val locationRequest = LocationRequest.Builder(INTERVAL_MILLS)
            .setIntervalMillis(INTERVAL_MILLS)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        // 위치 권한 확인 후 위치 업데이트 요청
        if (ActivityCompat.checkSelfPermission(
                this,
                permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // 위치 업데이트 요청
        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(locationRequest, mLocationCallback, Looper.getMainLooper())

        // 포그라운드 서비스 시작
        startForeground(Constants.LOCATION_SERVICE_ID, builder.build())
    }

    // 위치 서비스 중지
    private fun stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this)
            .removeLocationUpdates(mLocationCallback)
        stopForeground(true)
        stopSelf()
    }

    // 서비스가 시작될 때 호출 (액션에 따라 위치 서비스 시작/중지)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action
        if (action != null) {
            if (action == Constants.ACTION_START_LOCATION_SERVICE) {
                startLocationService()
            } else if (action == Constants.ACTION_STOP_LOCATION_SERVICE) {
                stopLocationService()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    companion object {
        const val INTERVAL_MILLS = 60 * 1000L // 1분
    }
}
