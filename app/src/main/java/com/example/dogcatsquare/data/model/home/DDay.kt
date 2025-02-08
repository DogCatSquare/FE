package com.example.dogcatsquare.data.model.home

data class DDay(
    val id: Int,
    val title: String,
    val day: String,
    val term: Int?,
    val daysLeft: Int,
    val ddayText: String,
    val ddayImageUrl: String
)
