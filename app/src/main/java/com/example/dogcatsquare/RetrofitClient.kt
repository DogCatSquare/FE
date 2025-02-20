package com.example.dogcatsquare

import android.util.Log
import com.example.dogcatsquare.data.api.PlacesApiService
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.api.WalkApiService
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://3.39.188.10:8080"
    private const val NAVER_GEOCODE_URL = "https://naveropenapi.apigw.ntruss.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .method(original.method, original.body)
                .build()

            try {
                chain.proceed(request)
            } catch (e: Exception) {
                // 타임아웃 발생 시 재시도
                if (e is SocketTimeoutException) {
                    try {
                        // 2초 대기 후 한 번 더 시도
                        Thread.sleep(2000)
                        return@addInterceptor chain.proceed(request)
                    } catch (retryException: Exception) {
                        Log.e("RetrofitClient", "Retry failed", retryException)
                        throw retryException
                    }
                }
                throw e
            }
        }
        .connectTimeout(30, TimeUnit.SECONDS)      // 연결 타임아웃 감소
        .readTimeout(30, TimeUnit.SECONDS)         // 읽기 타임아웃 감소
        .writeTimeout(30, TimeUnit.SECONDS)        // 쓰기 타임아웃 감소
        .retryOnConnectionFailure(true)           // 연결 실패시 재시도
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    val placesApiService: PlacesApiService by lazy {
        retrofit.create(PlacesApiService::class.java)
    }

    val userApiService: UserRetrofitItf by lazy {
        retrofit.create(UserRetrofitItf::class.java)
    }

    private val naverOkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-NCP-APIGW-API-KEY-ID", "pcpctqyg0l")
                .addHeader("X-NCP-APIGW-API-KEY", "KGksNR4S3hgDNRYOdzBHfRvgyw5Bh1oBTMEpRJ8K")
                .build()
            chain.proceed(request)
        }
        .build()

    private val naverRetrofit = Retrofit.Builder()
        .baseUrl(NAVER_GEOCODE_URL)
        .client(naverOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val naverGeocodeService: NaverGeocodeService by lazy {
        naverRetrofit.create(NaverGeocodeService::class.java)
    }

    val walkApiService: WalkApiService by lazy {
        retrofit.create(WalkApiService::class.java)
    }
}