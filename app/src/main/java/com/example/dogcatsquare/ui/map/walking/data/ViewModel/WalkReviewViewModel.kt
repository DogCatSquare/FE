package com.example.dogcatsquare.ui.map.walking.data.ViewModel

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val apiService = RetrofitClient.retrofit.create(WalkApiService::class.java)

    // 산책로 후기 조회
    fun getWalkReviews(walkId: Int) {
        apiService.getWalkReview(walkId).enqueue(createCallback())
    }

    // 산책로 후기 저장
    fun saveWalkReview(walkId: Long, reviewContent: String, imageUri: Uri?, context: Context) {
        // 후기 내용 설정
        val reviewCreateRequestDto = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            """{"content":"$reviewContent"}"""
        )

        // 이미지 URI가 있는 경우 이미지 파일 변환
        val imageParts: List<MultipartBody.Part> = if (imageUri != null) {
            val imageFile = File(getRealPathFromURI(imageUri, context))  // URI에서 파일 경로 얻기

            // 확장자 기반으로 MIME 타입 설정
            val fileExtension = imageFile.extension.lowercase()
            val mimeType = when (fileExtension) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "gif" -> "image/gif"
                else -> "image/*"
            }

            val imageRequestFile = RequestBody.create(mimeType.toMediaTypeOrNull(), imageFile)
            listOf(MultipartBody.Part.createFormData("walkReviewImages", imageFile.name, imageRequestFile))
        } else {
            emptyList() // 이미지가 없는 경우 빈 리스트 반환
        }

        // 로딩 상태 표시
        _isLoading.value = true

        // 서버에 후기 데이터 전송
        apiService.saveWalkReview(walkId, reviewCreateRequestDto, imageParts).enqueue(createCallback())
    }


    // 공통 콜백 함수
    private fun createCallback(): Callback<WalkReviewResponse> {
        return object : Callback<WalkReviewResponse> {
            override fun onResponse(
                call: Call<WalkReviewResponse>,
                response: Response<WalkReviewResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _reviewResponse.value = response.body()
                } else {
                    Log.e("WalkReview", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<WalkReviewResponse>, t: Throwable) {
                _isLoading.value = false
                Log.e("WalkReview", "Request failed: ${t.message}")
            }
        }
    }

    // URI에서 실제 파일 경로 얻는 함수
    private fun getRealPathFromURI(contentUri: Uri, context: Context): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(contentUri, proj, null, null, null)
        cursor?.moveToFirst()

        return try {
            val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val path = columnIndex?.let { cursor.getString(it) }
            cursor?.close()
            path ?: ""
        } catch (e: Exception) {
            Log.e("WalkReviewViewModel", "Error getting real path from URI: ${e.message}")
            cursor?.close()
            ""
        }
    }
}
