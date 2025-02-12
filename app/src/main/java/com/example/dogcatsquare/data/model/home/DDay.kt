package com.example.dogcatsquare.data.model.home

data class DDay(
    val id: Int,
    val title: String,
    val day: String,
    val term: Int?,
    val daysLeft: Int,
    val isAlarm: Boolean,
    val ddayText: String,
    val ddayImageUrl: String
)
 data class DDayCount (
     var count: Int
 )