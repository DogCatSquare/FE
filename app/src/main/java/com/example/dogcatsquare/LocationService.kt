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
import com.example.dogcatsquare.data.api.LocationUpdateInterface

class LocationService : Service() {

    private val binder = LocalBinder()
    private var locationInterface: LocationUpdateInterface? = null

    inner class LocalBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if (locationResult.lastLocation != null) {
                val latitude = locationResult.lastLocation!!.latitude
                val longitude = locationResult.lastLocation!!.longitude
                Log.v("LOCATION_UPDATE", "$latitude, $longitude")
                locationInterface?.sendLocation(latitude, longitude)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    fun setLocationUpdateInterface(locationInterface: LocationUpdateInterface) {
        this.locationInterface = locationInterface
        Log.d("LocationService", "setLocationUpdateInterface()")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ForegroundServiceType")
    private fun startLocationService() {
        val channelId = "location_notification_channel"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val resultIntent = Intent()
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            resultIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

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

        if (notificationManager.getNotificationChannel(channelId) == null) {
            val notificationChannel = NotificationChannel(
                channelId,
                "Location Service",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = "This channel is used by location service"
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val locationRequest = LocationRequest.Builder(INTERVAL_MILLS)
            .setIntervalMillis(INTERVAL_MILLS)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

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

        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(locationRequest, mLocationCallback, Looper.getMainLooper())

        startForeground(com.example.dogcatsquare.Constants.LOCATION_SERVICE_ID, builder.build())
    }

    private fun stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this)
            .removeLocationUpdates(mLocationCallback)
        stopForeground(true)
        stopSelf()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action
        if (action != null) {
            if (action == com.example.dogcatsquare.Constants.ACTION_START_LOCATION_SERVICE) {
                startLocationService()
            } else if (action == com.example.dogcatsquare.Constants.ACTION_STOP_LOCATION_SERVICE) {
                stopLocationService()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    companion object {
        const val INTERVAL_MILLS = 60 * 1000L // 1분
    }
}
