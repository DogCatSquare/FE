package com.example.dogcatsquare.ui.viewmodel

import WeatherViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class WeatherViewModelFactory(private val token: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
//            return WeatherViewModel(token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}