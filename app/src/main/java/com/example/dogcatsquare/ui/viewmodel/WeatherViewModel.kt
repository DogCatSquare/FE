import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import com.example.dogcatsquare.data.api.WeatherRetrofitItf
import com.example.dogcatsquare.data.model.home.WeatherResponse
import com.example.dogcatsquare.data.network.RetrofitObj

class WeatherViewModel(private val token: String) : ViewModel() {
    private val _weatherData = MutableLiveData<WeatherResponse?>()
    val weatherData: LiveData<WeatherResponse?> = _weatherData

    private var lastFetchTime: Long = 0
    private val CACHE_DURATION = 30 * 60 * 1000 // 30분

    fun fetchWeatherData(forceRefresh: Boolean = false) {
        val currentTime = System.currentTimeMillis()

        // ✅ 30분 이내에 호출된 경우, API 호출을 스킵
        if (!forceRefresh && _weatherData.value != null && (currentTime - lastFetchTime) < CACHE_DURATION) {
            return
        }

        viewModelScope.launch {
            try {
                val service = RetrofitObj.getRetrofit().create(WeatherRetrofitItf::class.java)
                val response = service.getWeather("Bearer $token").execute()

                if (response.isSuccessful) {
                    _weatherData.value = response.body()
                    lastFetchTime = currentTime // 마지막 호출 시간 업데이트
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
