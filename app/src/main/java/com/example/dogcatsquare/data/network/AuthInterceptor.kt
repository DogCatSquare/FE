import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthInterceptor(private val context: Context, private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()

        // ✅ No-Auth 헤더가 있으면 → 토큰 생략
        val noAuth = request.header("No-Auth")
        if (noAuth == "true") {
            request = request.newBuilder()
                .removeHeader("No-Auth") // 헤더 제거하고 요청 그대로 진행
                .build()
            return chain.proceed(request)
        }

        // ✅ accessToken 붙이기
        val accessToken = tokenManager.getAccessToken()
        request = request.newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        var response = chain.proceed(request)

        // ❗ 여기서 바로 response 닫지 말고 → 처리 후 필요 시 닫아야 함
        if (response.code != 401) {
            return response
        }

        Log.d("AuthInterceptor", "AccessToken 만료, Refresh 시도")

        // 응답은 사용 안 할 거니까 여기서 안전하게 닫아줌
        response.close()

        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrBlank()) {
            tokenManager.clear()
            // 새 응답 객체 생성하여 반환
            return response.newBuilder()
                .code(401)
                .message("Unauthorized - No refresh token")
                .build()
        }

        // 새 Retrofit 인스턴스로 refresh 요청
        val refreshService = Retrofit.Builder()
            .baseUrl("http://3.39.188.10:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserRetrofitItf::class.java)

        try {
            val refreshResponse = refreshService.refreshToken("Bearer $refreshToken").execute()

            if (refreshResponse.isSuccessful && refreshResponse.body()?.isSuccess == true) {
                val newAccessToken = refreshResponse.body()!!.result.accessToken
                val newRefreshToken = refreshResponse.body()!!.result.refreshToken

                tokenManager.saveTokens(newAccessToken, newRefreshToken)

                val newRequest = request.newBuilder()
                    .removeHeader("Authorization")
                    .addHeader("Authorization", "Bearer $newAccessToken")
                    .build()

                // 기존 응답은 닫고 새 요청으로 새 응답 생성
                response.close()
                return chain.proceed(newRequest)
            } else {
                Log.e("AuthInterceptor", "Refresh 실패 → 로그인 필요")
                tokenManager.clear()

                // 새 응답 객체 생성하여 반환
                return response.newBuilder()
                    .code(401)
                    .message("Unauthorized - Refresh failed")
                    .build()
            }
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Refresh 요청 중 오류 발생", e)

            // 새 응답 객체 생성하여 반환
            return response.newBuilder()
                .code(401)
                .message("Unauthorized - Error during refresh")
                .build()
        }
    }
}