package com.example.dogcatsquare.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel: ViewModel() {
    private val _profileImageUri = MutableLiveData<Uri>()
    val profileImageUri: LiveData<Uri> get() = _profileImageUri

    fun setProfileImageUri(uri: Uri) {
        _profileImageUri.value = uri
    }
}