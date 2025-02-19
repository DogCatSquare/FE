package com.example.dogcatsquare.ui.map.walking.data

import com.example.dogcatsquare.ui.map.walking.WalkApiService
import com.example.dogcatsquare.ui.map.walking.data.Request.Coordinate
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkResponse

class WalkRepository(private val apiService: WalkApiService) {
    suspend fun getWalkList(request: Coordinate): WalkResponse {
        return apiService.getWalkList(request)
    }
}
