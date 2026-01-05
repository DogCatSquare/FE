package com.example.dogcatsquare.ui.map.walking.data.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogcatsquare.data.network.RetrofitClient

// [수정] === 이것이 핵심입니다 ===
// 'data.api'의 WalkApiService가 아닌, 'ui.map.walking'의 WalkApiService를 임포트합니다.
import com.example.dogcatsquare.ui.map.walking.WalkApiService
// ===

import com.example.dogcatsquare.ui.map.walking.data.LatLngDto
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkReviewResponse
import com.example.dogcatsquare.ui.map.walking.data.WalkCreateRequestDto
import com.example.dogcatsquare.ui.map.walking.data.WalkCreateResponse
import com.google.gson.Gson

// [수정] Google Map의 LatLng를 임포트합니다.
import com.google.android.gms.maps.model.LatLng

import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WalkReviewViewModel : ViewModel() {

    private val _reviewResponse = MutableLiveData<WalkReviewResponse?>()
    val reviewResponse: LiveData<WalkReviewResponse?> get() = _reviewResponse

    private val _walkCreateResponse = MutableLiveData<WalkCreateResponse>()
    val walkCreateResponse: LiveData<WalkCreateResponse> = _walkCreateResponse

    // [수정]
    // RetrofitClient.walkApiService는 'data.api'의 잘못된 서비스를 참조하므로
    // 'ui.map.walking'의 올바른 서비스로 직접 인스턴스를 생성합니다.
    private val apiService = RetrofitClient.retrofit.create(WalkApiService::class.java)

    fun createWalk(
        token: String,
        // [수정] Google Map의 LatLng 타입으로 받음
        routeCoords: List<LatLng>,
        elapsedMinutes: Long,
        distance: Float,
        content: String,
        image: List<MultipartBody.Part>
    ) {
        viewModelScope.launch {
            try {
                // [수정] Google Map LatLng -> LatLngDto로 변환
                val coordinateDtos = routeCoords.map { gmsLatLng ->
                    LatLngDto(
                        latitude = gmsLatLng.latitude,
                        longitude = gmsLatLng.longitude
                    )
                }

                // [수정] DTO에 description 필드 포함 (WalkCreateRequestDto.kt 수정 필요)
                val walkCreateRequestDto = WalkCreateRequestDto(
                    coordinates = coordinateDtos,
                    duration = elapsedMinutes,
                    distance = distance,
                    description = content // (이전 단계에서 DTO에 description 필드를 추가했어야 합니다)
                )

                // [수정] createWalk는 suspend 함수이므로 .enqueue() 없이 직접 호출
                //
                val response = apiService.createWalk(
                    walkCreateRequestDto = walkCreateRequestDto,
                    walkReviewImages = image // API는 이미지 리스트(List)를 받음
                )

                _walkCreateResponse.postValue(response)

            } catch (e: Exception) {
                Log.e("WalkReviewViewModel", "Error creating walk", e)
            }
        }
    }


    // 산책로 후기 조회
    fun getWalkReviews(walkId: Int) {
        // [정상]
        // 'apiService'가 올바른 WalkApiService를 참조하게 되었으므로
        // .getWalkReview를 찾을 수 있습니다.
        apiService.getWalkReview(walkId).enqueue(object : Callback<WalkReviewResponse> {
            override fun onResponse(
                call: Call<WalkReviewResponse>,
                response: Response<WalkReviewResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        _reviewResponse.value = responseBody
                    } else {
                        Log.e("WalkReview", "Response body is null")
                    }
                } else {
                    Log.e("WalkReview", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<WalkReviewResponse>, t: Throwable) {
                Log.e("WalkReview", "Request failed: ${t.message}")
            }
        })
    }

}