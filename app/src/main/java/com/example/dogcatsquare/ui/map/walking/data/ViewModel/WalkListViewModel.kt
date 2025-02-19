package com.example.dogcatsquare.ui.map.walking.data.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogcatsquare.ui.map.walking.data.Request.Coordinate
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkResponse
import com.example.dogcatsquare.ui.map.walking.data.WalkRepository
import kotlinx.coroutines.launch

sealed class WalkListState {
    object Loading : WalkListState()
    data class Success(val walkList: WalkResponse) : WalkListState()
    data class Error(val message: String) : WalkListState()
}

class WalkListViewModel(private val repository: WalkRepository) : ViewModel() {

    private val _walkListState = MutableLiveData<WalkListState>()
    val walkListState: LiveData<WalkListState> = _walkListState

    fun fetchWalkList(latitude: Double, longitude: Double) {
        _walkListState.value = WalkListState.Loading

        viewModelScope.launch {
            try {
                val request = Coordinate(latitude, longitude, 0)
                val response = repository.getWalkList(request)
                _walkListState.value = WalkListState.Success(response)
            } catch (e: Exception) {
                _walkListState.value = WalkListState.Error("네트워크 오류: ${e.message}")
            }
        }
    }
}
