package com.example.dogcatsquare.ui.map.walking.data.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogcatsquare.data.network.RetrofitClient
import com.example.dogcatsquare.ui.map.walking.WalkApiService
import com.example.dogcatsquare.ui.map.walking.data.LatLngDto
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkReviewResponse
import com.example.dogcatsquare.ui.map.walking.data.WalkCreateRequestDto
import com.example.dogcatsquare.ui.map.walking.data.WalkCreateResponse
import com.google.gson.Gson
import com.naver.maps.geometry.LatLng
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

    private val walkApiService = RetrofitClient.walkApiService

    private val apiService = RetrofitClient.retrofit.create(WalkApiService::class.java)

    fun createWalk(
        token: String, // 토큰 추가
        routeCoords: List<LatLng>,
        elapsedMinutes: Long,
        distance: Float,
        content: String,
        image: MultipartBody.Part
    ) {
        viewModelScope.launch {
            try {
                // Authorization 헤더 추가
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"

                // WalkCreateRequestDto 생성
                val walkCreateRequestDto = WalkCreateRequestDto(
                    coordinates = routeCoords.mapIndexed { index, latLng ->
                        LatLngDto(latLng.latitude, latLng.longitude)
                    },
                    duration = elapsedMinutes,
                    distance = distance,
//                    description = content // 추가
                )

                // JSON 변환 및 RequestBody 생성
                val walkCreateRequestDtoJson = Gson().toJson(walkCreateRequestDto)
                val requestBody = walkCreateRequestDtoJson.toRequestBody("application/json".toMediaTypeOrNull())

            } catch (e: Exception) {
                Log.e("WalkReviewViewModel", "Error creating walk", e)
            }
        }
    }


    // 산책로 후기 조회
    fun getWalkReviews(walkId: Int) {
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

    // 산책로 후기 저장
//    fun saveWalkReview(walkId: Long, reviewContent: String, imageFilePath: String) {
//        val reviewCreateRequestDto = RequestBody.create(
//            "application/json".toMediaTypeOrNull(),
//            """{"content":"$reviewContent"}"""
//        )
//
//        val imageFile = File(imageFilePath)
//
//        val requestFile = RequestBody.create("image/png".toMediaTypeOrNull(), imageFile)
//        val imagePart = MultipartBody.Part.createFormData("walkReviewImages", imageFile.name, requestFile)
//
//        val imageParts: List<MultipartBody.Part> = listOf(imagePart)
//
//        apiService.saveWalkReview(walkId, reviewCreateRequestDto, imageParts).enqueue(object : Callback<WalkReviewResponse> {
//            override fun onResponse(
//                call: Call<WalkReviewResponse>,
//                response: Response<WalkReviewResponse>
//            ) {
//                if (response.isSuccessful) {
//                    val responseBody = response.body()
//                    if (responseBody != null) {
//                        _reviewResponse.value = responseBody
//                    } else {
//                        Log.e("WalkReview", "Response body is null")
//                    }
//                } else {
//                    Log.e("WalkReview", "Error: ${response.code()}")
//                }
//            }
//
//            override fun onFailure(call: Call<WalkReviewResponse>, t: Throwable) {
//                Log.e("WalkReview", "Request failed: ${t.message}")
//            }
//        })
//    }


}


