package com.example.dogcatsquare.ui.map.walking.data.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dogcatsquare.RetrofitClient
import com.example.dogcatsquare.ui.map.walking.WalkApiService
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkReviewResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

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

    // 산책로 후기 저장
    fun saveWalkReview(walkId: Long, reviewContent: String, imageFilePath: String) {
        val reviewCreateRequestDto = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            """{"content":"$reviewContent"}"""
        )

        val imageFile = File(imageFilePath)

        val requestFile = RequestBody.create("image/png".toMediaTypeOrNull(), imageFile)
        val imagePart = MultipartBody.Part.createFormData("walkReviewImages", imageFile.name, requestFile)

        val imageParts: List<MultipartBody.Part> = listOf(imagePart)

        apiService.saveWalkReview(walkId, reviewCreateRequestDto, imageParts).enqueue(object : Callback<WalkReviewResponse> {
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


