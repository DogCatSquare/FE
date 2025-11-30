package com.example.dogcatsquare.data.model.home

data class Alarm(
    val id: Long, // 알림 읽음 처리를 위해 반드시 필요
    val name: String,
    val content: String,
    val date: String
)
