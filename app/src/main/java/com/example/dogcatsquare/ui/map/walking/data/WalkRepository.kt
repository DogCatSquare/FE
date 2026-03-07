package com.example.dogcatsquare.ui.map.walking.data

import com.example.dogcatsquare.ui.map.walking.WalkingApiService
import com.example.dogcatsquare.ui.map.walking.data.Request.Coordinate
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkResponse

class WalkRepository(private val apiService: WalkingApiService) {
    suspend fun getWalkList(request: Coordinate): WalkResponse {
        return apiService.getWalkList(request)
    }
}
