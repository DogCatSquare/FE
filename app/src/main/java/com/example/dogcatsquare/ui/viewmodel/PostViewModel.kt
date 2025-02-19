package com.example.dogcatsquare.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PostViewModel : ViewModel() {
    private val _likedPosts = MutableLiveData<MutableMap<Int, Boolean>>() // postId -> 좋아요 상태
    val likedPosts: LiveData<MutableMap<Int, Boolean>> get() = _likedPosts

    init {
        _likedPosts.value = mutableMapOf()
    }

    fun updateLikeStatus(postId: Int, isLiked: Boolean) {
        val updatedMap = _likedPosts.value?.toMutableMap() ?: mutableMapOf()
        updatedMap[postId] = isLiked
        _likedPosts.value = updatedMap  // 변경된 데이터 반영하여 UI 갱신
    }
}
