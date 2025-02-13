package com.example.dogcatsquare.data.model.home

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class WeatherResponse(
    @SerializedName("isSuccess") var isSuccess: Boolean,
    @SerializedName("code") var code: String,
    @SerializedName("message") var message: String,
    @SerializedName("result") var result: WeatherResult
)

@Parcelize
data class WeatherResult(
    // 날씨 주요 정보
    @SerializedName("mainMessage") val mainMessage: String,   // 주요 날씨 메시지
    @SerializedName("subMessage") val subMessage: String,     // 반려동물 맞춤 메시지
    @SerializedName("location") val location: String,         // 위치 정보

    // 온도 정보
    @SerializedName("currentTemp") val currentTemp: String,   // 현재 기온
    @SerializedName("maxTemp") val maxTemp: String,           // 최고 기온
    @SerializedName("minTemp") val minTemp: String,           // 최저 기온

    // 날씨 추가 정보
    @SerializedName("imageUrl") val imageUrl: String,         // 날씨 상태 이미지 URL
    @SerializedName("rainProbability") val rainProbability: String, // 강수 확률

    // D-day 정보 (선택적)
    @SerializedName("ddayTitle") val ddayTitle: String?,      // D-day 제목 (null 가능)
    @SerializedName("ddayMessage") val ddayMessage: String?,  // D-day 메시지 (null 가능)
    @SerializedName("ddayDate") val ddayDate: String?         // D-day 날짜 (null 가능)
) : Parcelable