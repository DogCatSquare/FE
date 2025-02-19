package com.example.dogcatsquare.ui.map.walking.data.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dogcatsquare.RetrofitClient
import com.example.dogcatsquare.ui.map.walking.WalkApiService
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkReviewResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WalkReviewViewModel : ViewModel() {

    private val _reviewResponse = MutableLiveData<WalkReviewResponse?>()
    val reviewResponse: LiveData<WalkReviewResponse?> get() = _reviewResponse

    private val apiService = RetrofitClient.retrofit.create(WalkApiService::class.java)

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
}


