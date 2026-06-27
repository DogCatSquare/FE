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

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAlarmRecyclerView()
    }

    override fun onStart() {
        super.onStart()
    }

    private fun setupAlarmRecyclerView() {
        binding.alarmRv.layoutManager = LinearLayoutManager(requireContext())
        val adapter = AlarmRVAdapter(alarmDatas) { alarmId ->
            Log.d("AlarmFragment", "Item clicked! alarmId = $alarmId")
            val clickedAlarm = alarmDatas.find { it.id == alarmId }
            Log.d("AlarmFragment", "Found clickedAlarm = $clickedAlarm")
            clickedAlarm?.let { alarm ->
                navigateToDetail(alarm)
            }
            markAlarmAsRead(alarmId)
        }
        binding.alarmRv.adapter = adapter

        val mainActivity = requireActivity() as com.example.dogcatsquare.MainActivity
        mainActivity.alarmList.observe(viewLifecycleOwner) { alarms ->
            Log.d("AlarmFragment", "Received alarmList update: size = ${alarms.size}")
            alarmDatas.clear()
            alarmDatas.addAll(alarms)
            adapter.notifyDataSetChanged()
        }
    }

    private fun navigateToDetail(alarm: Alarm) {
        val type = alarm.type?.uppercase()
        val targetId = alarm.targetId
        Log.d("AlarmFragment", "navigateToDetail: type = $type, targetId = $targetId")



        if (type == null) {
            Log.w("AlarmFragment", "Cannot navigate: type is null!")
            return
        }

        when (type) {
            "NOTICE" -> {
                if (targetId != null) {
                    Log.d("AlarmFragment", "Navigating to AnnouncementDetailFragment with noticeId = $targetId")
                    val fragment = com.example.dogcatsquare.ui.mypage.AnnouncementDetailFragment.newInstance(targetId)
                    parentFragmentManager.beginTransaction()
                        .replace(com.example.dogcatsquare.R.id.main_frm, fragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
                } else {
                    Log.w("AlarmFragment", "Cannot navigate: targetId is null for NOTICE")
                }
            }
            "COMMENT", "LIKE" -> {
                if (targetId != null) {
                    Log.d("AlarmFragment", "Navigating to PostDetailActivity with postId = $targetId")
                    val intent = android.content.Intent(requireContext(), com.example.dogcatsquare.ui.community.PostDetailActivity::class.java).apply {
                        putExtra("postId", targetId.toInt())
                    }
                    startActivity(intent)
                } else {
                    Log.w("AlarmFragment", "Cannot navigate: targetId is null for $type")
                }
            }
            "DDAY", "TEST" -> {
                Log.d("AlarmFragment", "No navigation for $type")
            }
            else -> {
                Log.w("AlarmFragment", "Unknown alarm type: $type")
            }
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
                        
                        val mainActivity = requireActivity() as? com.example.dogcatsquare.MainActivity
                        mainActivity?.let { activity ->
                            val currentCount = activity.unreadCount.value ?: 0
                            val reduction = alarmIds.size
                            activity.unreadCount.value = (currentCount - reduction).coerceAtLeast(0)

                            val currentList = activity.alarmList.value.orEmpty().toMutableList()
                            currentList.removeAll { it.id in alarmIds }
                            activity.alarmList.value = currentList
                        }
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