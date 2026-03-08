// com.example.dogcatsquare.ui.home.AlarmFragment.kt

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.data.model.home.Alarm
import com.example.dogcatsquare.databinding.FragmentAlarmBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
// 필요한 API Service와 Request 모델 import (아래에 생성 가정)
import com.example.dogcatsquare.data.api.AlarmApiService
import com.example.dogcatsquare.data.model.alarm.ReadNotificationRequest
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.ui.home.AlarmRVAdapter
import androidx.lifecycle.lifecycleScope
import com.example.dogcatsquare.data.api.SseAlarmService
import com.example.dogcatsquare.data.model.alarm.SSEAlarmResponse
import com.example.dogcatsquare.data.sse.SSEClient
import kotlinx.coroutines.flow.collectLatest
import okhttp3.OkHttpClient // OkHttpClient import

class AlarmFragment : Fragment() {
    private lateinit var binding: FragmentAlarmBinding
    private var alarmDatas = ArrayList<Alarm>()
    private val alarmApiService: AlarmApiService by lazy {
        RetrofitObj.getRetrofit(requireContext()).create(AlarmApiService::class.java)
    }

    private fun getToken(): String? {
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

    // SSEClient는 MainActivity의 전역 sseClient를 사용합니다.

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAlarmRecyclerView()
        observeSSEEvents() // SSE 이벤트 관찰 시작
    }

    override fun onStart() {
        super.onStart()

        // SSE 관리는 이제 MainActivity에서 하므로, 별도 시작 로직이 없습니다.
        // 알림 목록 화면으로 들어왔을 때 전체 읽음 처리
    }

    private fun setupAlarmRecyclerView() {
        binding.alarmRv.layoutManager = LinearLayoutManager(requireContext())
        val adapter = AlarmRVAdapter(alarmDatas) { alarmId ->
            // RV Adapter의 클릭 리스너를 통해 알림 ID를 받아 개별 읽음 처리 API 호출
            markAlarmAsRead(alarmId)
        }
        binding.alarmRv.adapter = adapter
    }

    private fun observeSSEEvents() {
        // lifecycleScope를 사용하여 Fragment의 생명주기에 맞게 코루틴 실행
        val mainActivity = requireActivity() as com.example.dogcatsquare.MainActivity
        viewLifecycleOwner.lifecycleScope.launch {
            mainActivity.sseClient.sseEvents.collectLatest { sseAlarm ->
                handleNewSSEAlarm(sseAlarm)
            }
        }
    }

    private fun handleNewSSEAlarm(sseAlarm: SSEAlarmResponse) {
        // SSEAlarmResponse를 Alarm 데이터 모델로 변환
        val formattedDate = com.example.dogcatsquare.util.DateFmt.format(sseAlarm.createdAt).replace(".", "-")
        val newAlarm = Alarm(
            id = sseAlarm.id,
            name = sseAlarm.type ?: "새로운 알림",
            content = sseAlarm.content,
            date = formattedDate
        )

        // UI 업데이트는 Main Dispatcher에서 처리
        lifecycleScope.launch(Dispatchers.Main) {
            // 새 알림을 목록의 맨 앞에 추가
            alarmDatas.add(0, newAlarm)
            binding.alarmRv.adapter?.notifyItemInserted(0)
            binding.alarmRv.scrollToPosition(0) // 맨 위로 스크롤
        }
    }

    private fun markAlarmAsRead(vararg alarmIds: Long) {
        if (alarmIds.isEmpty()) return

        val request = ReadNotificationRequest(ids = alarmIds.toList())
        Log.d("AlarmFragment", "알림 읽음 처리 요청: IDs ${alarmIds.joinToString()}")

        // API 호출은 IO Dispatcher에서 처리
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // AlarmApiService.kt의 markNotificationsAsRead를 호출
                val response = alarmApiService.markNotificationsAsRead(request)

                if (response.isSuccessful) {
                    // UI 업데이트는 Main Dispatcher에서 처리
                    lifecycleScope.launch(Dispatchers.Main) {
                        Log.d("AlarmFragment", "알림 읽음 처리 성공 (ID: ${alarmIds.joinToString()})")
                        Toast.makeText(requireContext(), "알림을 읽음 처리했습니다.", Toast.LENGTH_SHORT).show()
                        // TODO: UI에서 해당 알림을 "읽음" 상태로 업데이트하는 로직 추가
                    }
                } else {
                    Log.e("AlarmFragment", "알림 읽음 처리 실패: ${response.code()} ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("AlarmFragment", "알림 읽음 처리 중 예외 발생: ${e.message}", e)
            }
        }
    }
}