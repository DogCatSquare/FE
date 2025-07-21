package com.example.dogcatsquare.ui.map.walking.data.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dogcatsquare.data.network.RetrofitClient
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkDetail
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkDetailResponse
import com.example.dogcatsquare.ui.map.walking.WalkApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

sealed class WalkDetailState {
    object Loading : WalkDetailState()  // 로딩 중
    data class Success(val walkDetail: WalkDetail) : WalkDetailState()  // 성공한 경우
    data class Error(val message: String) : WalkDetailState()  // 실패한 경우
}

class WalkDetailViewModel : ViewModel() {

    private val _walkDetailState = MutableLiveData<WalkDetailState>()
    val walkDetailState: LiveData<WalkDetailState> get() = _walkDetailState

    private val apiService = RetrofitClient.retrofit.create(WalkApiService::class.java)

    // 특정 산책로 상세 조회 함수
    fun fetchWalkDetail(walkId: Int) {
        apiService.getWalkDetail(walkId).enqueue(object : Callback<WalkDetailResponse> {
            override fun onResponse(call: Call<WalkDetailResponse>, response: Response<WalkDetailResponse>) {
                if (response.isSuccessful) {
                    val walkDetail = response.body()?.result
                    if (walkDetail != null) {
                        Log.d("API_CALL", "API 호출 성공: $walkDetail")
                        _walkDetailState.value = WalkDetailState.Success(walkDetail)
                    } else {
                        Log.e("API_CALL", "API 호출 실패: 응답 본문이 null")
                        _walkDetailState.value = WalkDetailState.Error("응답 본문이 비어 있음")
                    }
                } else {
                    Log.e("API_CALL", "API 호출 실패: ${response.message()}")
                    _walkDetailState.value = WalkDetailState.Error(response.message())
                }
            }

            override fun onFailure(call: Call<WalkDetailResponse>, t: Throwable) {
                Log.e("API_CALL", "API 호출 실패: ${t.message}")
                _walkDetailState.value = WalkDetailState.Error(t.message ?: "알 수 없는 오류")
            }
        })
    }
}
