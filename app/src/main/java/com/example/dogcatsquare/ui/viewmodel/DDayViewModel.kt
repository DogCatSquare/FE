package com.example.dogcatsquare.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DDayViewModel : ViewModel() {
    private val _isAlarm = MutableLiveData<Boolean>()
    val isAlarm: LiveData<Boolean> get() = _isAlarm

    fun setAlarmState(state: Boolean) {
        _isAlarm.value = state
    }
}