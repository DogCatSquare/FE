import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import com.example.dogcatsquare.data.api.WeatherRetrofitItf
import com.example.dogcatsquare.data.model.home.WeatherResponse
import com.example.dogcatsquare.data.network.RetrofitObj
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val _weatherData = MutableLiveData<WeatherResponse?>()
    val weatherData: LiveData<WeatherResponse?> = _weatherData

    private var lastFetchTime: Long = 0
    private val CACHE_DURATION = 30 * 60 * 1000 // 30분

    private val token: String? = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        .getString("token", null)

    fun getWeatherData(forceRefresh: Boolean = false) {
        val currentTime = System.currentTimeMillis()

        if (!forceRefresh && _weatherData.value != null && (currentTime - lastFetchTime) < CACHE_DURATION) {
            Log.d("WeatherViewModel", "Cache 사용: ${currentTime - lastFetchTime}ms 경과")
            return
        }

        if (token.isNullOrBlank()) {
            Log.e("WeatherViewModel", "Token is null or blank")
            return
        }

        val weatherService = RetrofitObj.getRetrofit(getApplication()).create(WeatherRetrofitItf::class.java)
        weatherService.getWeather("Bearer $token").enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { weatherResponse ->
                        if (weatherResponse.isSuccess) {
                            _weatherData.value = weatherResponse
                            lastFetchTime = currentTime
                            Log.d("WeatherViewModel", "API 요청 성공: ${weatherResponse.result.mainMessage}")
                        } else {
                            Log.e("WeatherViewModel", "API 오류: ${weatherResponse.message}")
                        }
                    }
                } else {
                    Log.e("WeatherViewModel", "API 실패: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e("WeatherViewModel", "API 요청 실패: ${t.message}")
            }
        })
    }
}