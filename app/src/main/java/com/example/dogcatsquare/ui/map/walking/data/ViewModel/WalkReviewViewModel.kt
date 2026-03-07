package com.example.dogcatsquare.ui.map.walking.data.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dogcatsquare.data.network.RetrofitClient
import com.example.dogcatsquare.ui.map.walking.WalkApiService
import com.example.dogcatsquare.ui.map.walking.data.CoordinateDto
import com.example.dogcatsquare.ui.map.walking.data.Response.WalkReviewResponse
import com.example.dogcatsquare.ui.map.walking.data.SpecialDto
import com.example.dogcatsquare.ui.map.walking.data.WalkCreateRequestDto
import com.example.dogcatsquare.ui.map.walking.data.WalkCreateResponse
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
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

    private val apiService = RetrofitClient.retrofit.create(WalkApiService::class.java)

    var tempToken: String = ""
    var tempRouteCoords: List<LatLng> = emptyList()
    var tempElapsedMinutes: Long = 0L
    var tempDistance: Float = 0f
    var tempContent: String = ""
    var tempImages: List<MultipartBody.Part> = emptyList()
    var tempPlaceName: String = ""

    fun createWalk(
        difficulty: String,
        specialList: List<SpecialDto>
    ) {
        viewModelScope.launch {
            try {
                val coordinateDtos = tempRouteCoords.mapIndexed { index, gmsLatLng ->
                    CoordinateDto(
                        latitude = gmsLatLng.latitude,
                        longitude = gmsLatLng.longitude,
                        sequence = index
                    )
                }

                val walkCreateRequestDto = WalkCreateRequestDto(
                    title = tempPlaceName,
                    description = tempContent,
                    time = tempElapsedMinutes.toInt(),
                    distance = tempDistance.toInt(),
                    difficulty = difficulty,
                    special = specialList,
                    coordinates = coordinateDtos
                )

                val jsonString = Gson().toJson(walkCreateRequestDto)
                val requestBody = jsonString.toRequestBody("application/json".toMediaTypeOrNull())

                val dtoPart = MultipartBody.Part.createFormData("walkCreateRequestDto", null, requestBody)

                val response = apiService.createWalk(
                    token = tempToken,
                    walkCreateRequestDto = dtoPart,
                    walkReviewImages = tempImages
                )

                _walkCreateResponse.postValue(response)

            } catch (e: Exception) {
                Log.e("WalkReviewViewModel", "Error creating walk", e)
            }
        }
    }

    fun getWalkReviews(walkId: Int) {
        apiService.getWalkReview(walkId).enqueue(object : Callback<WalkReviewResponse> {
            override fun onResponse(call: Call<WalkReviewResponse>, response: Response<WalkReviewResponse>) {
                if (response.isSuccessful) {
                    _reviewResponse.value = response.body()
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