package com.example.dogcatsquare

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naver.maps.geometry.LatLng

class LocationViewModel : ViewModel() {
    private val _currentLocation = MutableLiveData<LatLng>()
    val currentLocation: LiveData<LatLng> = _currentLocation

    fun updateLocation(location: LatLng) {
        _currentLocation.value = location
    }
}