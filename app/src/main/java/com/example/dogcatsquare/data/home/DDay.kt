package com.example.dogcatsquare.data.home

data class DDay(
    val d_title: String?,
    val d_when: String?,
    val d_image: Int? = null,
    val isAddButton: Boolean = false // 디데이 추가 버튼 여부
)
