package com.example.dogcatsquare.ui.map.walking.data.ViewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dogcatsquare.RetrofitClient
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkDetail
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkDetailResponse
import com.example.dogcatsquare.ui.map.walking.WalkApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WalkDetailViewModel : ViewModel() {

    private val _walkDetail = MutableLiveData<WalkDetail?>()
    val walkDetail: MutableLiveData<WalkDetail?> get() = _walkDetail

    private val apiService = RetrofitClient.retrofit.create(WalkApiService::class.java)

    // 특정 산책로 상세 조회 함수
    fun fetchWalkDetail(walkId: Long) {
        apiService.getWalkDetail(walkId).enqueue(object : Callback<WalkDetailResponse> {
            override fun onResponse(
                call: Call<WalkDetailResponse>,
                response: Response<WalkDetailResponse>
            ) {
                if (response.isSuccessful) {
                    _walkDetail.value = response.body()?.result
                } else {
                    Log.e("WalkDetail", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<WalkDetailResponse>, t: Throwable) {
                Log.e("WalkDetail", "Request failed: ${t.message}")
            }
        })
    }
}
