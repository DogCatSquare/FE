package com.example.dogcatsquare.ui.map.walking.data.ViewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dogcatsquare.RetrofitClient
import com.example.dogcatsquare.ui.map.walking.WalkApiService
import com.example.dogcatsquare.ui.map.walking.data.Request.WalkReviewRequest
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkReviewResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WalkReviewViewModel : ViewModel() {

    private val _reviewResponse = MutableLiveData<WalkReviewResponse?>()
    val reviewResponse: MutableLiveData<WalkReviewResponse?> get() = _reviewResponse

    private val apiService = RetrofitClient.retrofit.create(WalkApiService::class.java)

    // 후기 제출 함수
    fun submitWalkReview(walkId: Long, content: String, imageBase64: String?) {
        val reviewRequest = WalkReviewRequest(content)

        // 비동기 요청 (enqueue)
        apiService.submitWalkReview(walkId, reviewRequest).enqueue(object : Callback<WalkReviewResponse> {
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

