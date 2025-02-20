package com.example.dogcatsquare.ui.home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.api.DDayRetrofitItf
import com.example.dogcatsquare.data.model.home.DDay
import com.example.dogcatsquare.data.model.home.DeleteDDayResponse
import com.example.dogcatsquare.data.model.home.FetchDDayRequest
import com.example.dogcatsquare.data.model.home.FetchDDayResponse
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.FragmentSetDDayPersonalBinding
import com.example.dogcatsquare.ui.viewmodel.DDayViewModel
import com.example.dogcatsquare.utils.AlarmHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class SetDDayPersonalFragment : Fragment() {
    lateinit var binding: FragmentSetDDayPersonalBinding

    private var dayId: Int = -1
    private var dayTitle: String = ""
    private var dayDay: String = ""
    private var dayTerm: Int =  -1
    private var isAlarm: Boolean = true

    private val dDayViewModel: DDayViewModel by viewModels()

    private fun getToken(): String? {
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetDDayPersonalBinding.inflate(inflater, container, false)

        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)

        // ✅ ViewModel과 UI 연동
        dDayViewModel.isAlarm.observe(viewLifecycleOwner) { isAlarm ->
            binding.alarmBtn.isChecked = isAlarm
        }

        // 배경화면 클릭 시 키보드 숨기기
        binding.setDDayPersonalFragment.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN){
                requireActivity().currentFocus?.let { view ->
                    val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    view.clearFocus()
                }
            }
            false // 터치 이벤트를 소비하지 않음
        }

        // 초기 설정
        arguments?.let {
            dayId = it.getInt("ddayId", -1)
            dayTitle = it.getString("ddayTitle", "")
            dayDay = it.getString("ddayDay", "")
            dayTerm = it.getInt("ddayTerm", -1)
            isAlarm = it.getBoolean("isAlarm", true)

            dDayViewModel.setAlarmState(isAlarm)
        }

        binding.dayTitle.text = dayTitle
        binding.countText.text = "${dayTerm}주"
        binding.dateBtn.text = dayDay
        binding.alarmBtn.isChecked = isAlarm

        binding.dateBtn.setOnClickListener { // 원하는 버튼 ID로 변경
            showCustomCalendarBottomSheet { selectedpadDate ->
                Toast.makeText(requireContext(), "선택한 날짜: $selectedpadDate", Toast.LENGTH_SHORT).show()
                binding.dateBtn.text = selectedpadDate
            }
        }

        // 구매 주기 설정
        setWeek()

        binding.alarmBtn.setOnCheckedChangeListener { _, isChecked ->
            dDayViewModel.setAlarmState(isChecked) // ✅ ViewModel 업데이트

            if (isChecked) {
                // 패드 구매 알람 활성화
                Toast.makeText(requireContext(), "알람이 설정되었습니다", Toast.LENGTH_SHORT).show()
                AlarmHelper.setDdayAlarm(requireContext(), DDay(dayId, dayTitle, dayDay, dayTerm, 0, true, "", ""))
            } else {
                // 패드 구매 알람 비활성화
                Toast.makeText(requireContext(), "알람이 해제되었습니다", Toast.LENGTH_SHORT).show()
                AlarmHelper.cancelDdayAlarm(requireContext(), dayId)
            }
        }

        // 수정 완료
        binding.fetchDayBtn.setOnClickListener {
            val day = binding.dateBtn.text.toString()
            val isAlarm = dDayViewModel.isAlarm.value ?: true
            setDDay(dayId, day, dayTerm, isAlarm)
        }

        binding.deleteDayBtn.setOnClickListener {
            deleteDDay(dayId)
        }

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    // 캘린더 뷰 바텀시트
    private fun showCustomCalendarBottomSheet(onDateSelected: (String) -> Unit) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_pick_buy_date, null)
        bottomSheetDialog.setContentView(view)

        val yearButton: MaterialButton = view.findViewById(R.id.year_button)
        val monthButton: MaterialButton = view.findViewById(R.id.month_button)
        val calendarGrid: GridLayout = view.findViewById(R.id.calendar_grid)
        val confirmButton: Button = view.findViewById(R.id.confirm_button)

        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance() // 오늘 날짜를 기준으로 설정
        var selectedYear = calendar.get(Calendar.YEAR)
        var selectedMonth = calendar.get(Calendar.MONTH) // 0부터 시작
        var selectedDay: Int? = null // 선택된 일자 저장 변수

        // 초기 연도와 월 설정
        yearButton.text = "$selectedYear"
        monthButton.text = "${selectedMonth + 1}월"

        // 캘린더 업데이트 함수
        fun updateCalendar(year: Int, month: Int) {
            calendarGrid.removeAllViews()
            val calendarInstance = Calendar.getInstance()
            calendarInstance.set(year, month, 1)

            // 첫 날의 요일 (일요일: 1, 월요일: 2, ...)
            val firstDayOfWeek = calendarInstance.get(Calendar.DAY_OF_WEEK)
            val daysInMonth = calendarInstance.getActualMaximum(Calendar.DAY_OF_MONTH)

            // 빈칸 추가 (첫 주의 시작 요일까지)
            for (i in 1 until firstDayOfWeek) {
                val emptyView = TextView(requireContext())
                emptyView.layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
                calendarGrid.addView(emptyView)
            }

            // 날짜 추가
            for (day in 1..daysInMonth) {
                val dateView = TextView(requireContext())
                dateView.text = day.toString()
                dateView.textSize = 16f
                dateView.gravity = android.view.Gravity.CENTER
                dateView.setPadding(8, 8, 8, 8)
                dateView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray3))

                dateView.layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }

                // 선택 불가 조건 추가
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, day)
                }

                dateView.setOnClickListener {
                    // 선택된 날짜 업데이트
                    selectedDay = day

                    // 이전 선택된 날짜 초기화
                    for (i in 0 until calendarGrid.childCount) {
                        val child = calendarGrid.getChildAt(i)
                        if (child is TextView) {
                            child.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                            child.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray3))
                        }
                    }

                    dateView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.main_color1))
                    dateView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    Toast.makeText(requireContext(), "${year}년 ${month + 1}월 ${day}일 선택됨", Toast.LENGTH_SHORT).show()
                }

                calendarGrid.addView(dateView)
            }
        }

        // 초기 캘린더 로드
        updateCalendar(selectedYear, selectedMonth)

        // 연도 선택 버튼 클릭
        yearButton.setOnClickListener {
            val years = (2020..2030).toList().map { it.toString() }.toTypedArray()
            showDropdownDialog("연도 선택", years) { year ->
                selectedYear = year.toInt()
                yearButton.text = year
                updateCalendar(selectedYear, selectedMonth)
            }
        }

        // 월 선택 버튼 클릭
        monthButton.setOnClickListener {
            val months = (1..12).toList().map { "${it}월" }.toTypedArray()
            showDropdownDialog("월 선택", months) { month ->
                selectedMonth = month.removeSuffix("월").toInt() - 1
                monthButton.text = month
                updateCalendar(selectedYear, selectedMonth)
            }
        }

        // 완료 버튼 클릭 이벤트
        confirmButton.setOnClickListener {
            val formattedMonth = String.format("%02d", selectedMonth + 1) // 01~09 변환
            val formattedDay = selectedDay?.let { String.format("%02d", it) } ?: "01" // 01~09 변환 (선택된 날짜 없을 때 기본값 01)
            val selectedDate = "${selectedYear}-${formattedMonth}-${formattedDay}"
            onDateSelected(selectedDate)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun showDropdownDialog(title: String, items: Array<String>, onItemSelected: (String) -> Unit) {
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle(title)
        builder.setItems(items) { _, which ->
            onItemSelected(items[which])
        }
        builder.show()
    }

    private fun setWeek() {
        var count = 1 // 초기 값 설정

        // 감소 버튼 클릭
        binding.decreaseBtn.setOnClickListener {
            if (count > 1) { // 최소값 설정
                count--
                binding.countText.text = "${count}주"
            }
        }

        // 증가 버튼 클릭
        binding.increaseBtn.setOnClickListener {
            if (count < 12) { // 최대값 설정
                count++
                binding.countText.text = "${count}주"
            }
        }
        dayTerm = count
    }

    private fun setDDay(id: Int, day: String, term: Int,  isAlarm: Boolean) {
        val token = getToken()

        val fetchDDayService = RetrofitObj.getRetrofit().create(DDayRetrofitItf::class.java)
        fetchDDayService.fetchDDay("Bearer $token", id, FetchDDayRequest(day, term, isAlarm)).enqueue(object:
            Callback<FetchDDayResponse> {
            override fun onResponse(call: Call<FetchDDayResponse>, response: Response<FetchDDayResponse>) {
                Log.d("RETROFIT/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    response.body()?.let { resp ->
                        if (resp.isSuccess) {
                            Log.d("FetchDDay/SUCCESS", "FetchDDay")

                            Toast.makeText(context, "디데이 수정이 완료되었습니다", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()

                            // ✅ ViewModel 업데이트
                            dDayViewModel.setAlarmState(isAlarm)

                            // ✅ 사용자가 설정한 isAlarm 값에 따라 알람 설정 또는 취소
                            if (isAlarm) {
                                AlarmHelper.setDdayAlarm(requireContext(), DDay(id, dayTitle, day, term, 0, true, "", ""))
                            } else {
                                AlarmHelper.cancelDdayAlarm(requireContext(), id)
                            }
                        } else {
                            Log.e(
                                "FetchDDay/FAILURE",
                                "응답 코드: ${resp.code}, 응답 메시지: ${resp.message}"
                            )
                            Toast.makeText(context, "오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<FetchDDayResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }

        })
    }

    private fun deleteDDay(id: Int) {
        val token = getToken()

        val deleteDDayService = RetrofitObj.getRetrofit().create(DDayRetrofitItf::class.java)
        deleteDDayService.deleteDDay("Bearer $token", id).enqueue(object : Callback<DeleteDDayResponse> {
            override fun onResponse(call: Call<DeleteDDayResponse>, response: Response<DeleteDDayResponse>) {
                Log.d("RETROFIT/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    response.body()?.let { resp ->
                        if (resp.isSuccess) {
                            Log.d("DeleteDay/SUCCESS", "Delete Day")

                            Toast.makeText(context, "디데이 정보가 삭제되었습니다", Toast.LENGTH_SHORT).show()
                            parentFragmentManager.popBackStack()

                            // ✅ 디데이가 삭제되었으므로 알람 취소
                            AlarmHelper.cancelDdayAlarm(requireContext(), id)
                        } else {
                            Log.e("DeleteDay/FAILURE", "응답 코드: ${resp.code}, 응답 메시지: ${resp.message}")
                            Toast.makeText(context, "오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<DeleteDDayResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }

        })
    }
}