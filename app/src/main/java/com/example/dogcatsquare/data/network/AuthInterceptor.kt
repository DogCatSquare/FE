import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.dogcatsquare.data.api.UserRetrofitItf
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthInterceptor(
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val response = chain.proceed(originalRequest)

        return when (response.code) {
            401 -> {
                Log.d("AuthInterceptor", "401 Unauthorized - Trying to refresh token")

                response.close() // 기존 응답 닫기
                val newAccessToken = runBlocking { getUpdatedToken() }

                return if (newAccessToken != null) {
                    Log.d("AuthInterceptor", "Successfully refreshed token: $newAccessToken")

                    // 새로운 요청 생성
                    val newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $newAccessToken")
                        .build()

                    chain.proceed(newRequest) // 새로운 요청 수행
                } else {
                    Log.e("AuthInterceptor", "Token refresh failed, returning original response")
                    response // 토큰 갱신 실패 시 기존 401 응답 반환
                }
            }
            else -> response
        }
    }

    private suspend fun getUpdatedToken(): String? {
        val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val refreshToken = sharedPreferences.getString("refreshToken", null) ?: return null

        Log.d("AuthInterceptor", "Refreshing token with refreshToken: $refreshToken")

        return try {
            val retrofit = Retrofit.Builder()
                .baseUrl("http://3.39.188.10:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(UserRetrofitItf::class.java)
            val response = api.refreshToken(refreshToken).execute()

            if (response.isSuccessful && response.body()?.isSuccess == true) {
                val newAccessToken = response.body()?.result?.accessToken

                sharedPreferences.edit().apply {
                    putString("accessToken", newAccessToken)
                    putString("refreshToken", response.body()?.result?.refreshToken)
                    apply()
                }

                Log.d("AuthInterceptor", "New access token saved: $newAccessToken")
                newAccessToken
            } else {
                Log.e("AuthInterceptor", "Token refresh request failed: ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Exception while refreshing token: ${e.message}")
            null
        }
    }
}