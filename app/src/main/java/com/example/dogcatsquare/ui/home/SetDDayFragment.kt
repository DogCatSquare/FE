package com.example.dogcatsquare.ui.home

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentSetDDayBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import java.util.Calendar

class SetDDayFragment : Fragment() {
    lateinit var binding: FragmentSetDDayBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetDDayBinding.inflate(inflater, container, false)

        // 상단바 색깔
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)

        // 배경화면 클릭 시 키보드 숨기기
        binding.setDDayFragment.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN){
                requireActivity().currentFocus?.let { view ->
                    val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    view.clearFocus()
                }
            }
            false // 터치 이벤트를 소비하지 않음
        }

        // 사료 주문 캘린더 설정
        binding.feedstuffBuyDateBtn.setOnClickListener { // 원하는 버튼 ID로 변경
            showCustomCalendarBottomSheet { selectedpadDate ->
                Toast.makeText(requireContext(), "선택한 날짜: $selectedpadDate", Toast.LENGTH_SHORT).show()
                binding.feedstuffBuyDateBtn.text = selectedpadDate
            }
        }

        // 구매 주기 설정
        setFoodWeek()

        binding.feedstuffBuyAlarmBtn.setOnClickListener {
            switchToggle()
        }

        // 패드 주문 캘린더 설정
        binding.padsBuyDateBtn.setOnClickListener { // 원하는 버튼 ID로 변경
            showCustomCalendarBottomSheet { selectedpadDate ->
                Toast.makeText(requireContext(), "선택한 날짜: $selectedpadDate", Toast.LENGTH_SHORT).show()
                binding.padsBuyDateBtn.text = selectedpadDate
            }
        }

        // 구매 주기 설정
        setPadWeek()

        binding.padsBuyAlarmBtn.setOnClickListener {
            switchToggle()
        }

        // 병원 방문 캘린더 설정
        binding.selectHospitalDateBtn.setOnClickListener { // 원하는 버튼 ID로 변경
            showCustomCalendarBottomSheet { selectedpadDate ->
                Toast.makeText(requireContext(), "선택한 날짜: $selectedpadDate", Toast.LENGTH_SHORT).show()
                binding.selectHospitalDateBtn.text = selectedpadDate
            }
        }

        // 병원 방문 추가
//        setHospitalWeek()

        binding.hospitalAlarmBtn.setOnClickListener {
            switchToggle()
        }

        // 직접 추가
        binding.addDayBtn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, AddDDayFragment())
                .addToBackStack("HomeFragment")
                .commitAllowingStateLoss()
        }

        // 수정 완료
        binding.setDayDoneBtn.setOnClickListener {
            // 수정한 데이터 전송하면서 메인으로 이동
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, HomeFragment())
                .commitAllowingStateLoss()
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

                if (selectedDate.after(today)) {
                    // 미래 날짜는 비활성화
                    dateView.isClickable = false // 클릭 불가
                } else {
                    dateView.setOnClickListener {
                        // 선택된 날짜 업데이트
                        selectedDay = day

                        // 이전 선택된 날짜 초기화
                        for (i in 0 until calendarGrid.childCount) {
                            val child = calendarGrid.getChildAt(i)
                            if (child is TextView) {
                                child.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray3))
                            }
                        }

                        dateView.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color1))

                        Toast.makeText(requireContext(), "${year}년 ${month + 1}월 ${day}일 선택됨", Toast.LENGTH_SHORT).show()
                    }
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
            val selectedDate = "${selectedYear}-${selectedMonth + 1}-${selectedDay}"
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

    private fun setFoodWeek() {
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
    }

    private fun setPadWeek() {
        var count = 1 // 초기 값 설정

        // 감소 버튼 클릭
        binding.padsDecreaseBtn.setOnClickListener {
            if (count > 1) { // 최소값 설정
                count--
                binding.padsCountText.text = "${count}주"
            }
        }

        // 증가 버튼 클릭
        binding.padsIncreaseBtn.setOnClickListener {
            if (count < 12) { // 최대값 설정
                count++
                binding.padsCountText.text = "${count}주"
            }
        }
    }

    private fun switchToggle() {
        // 알람 설정
    }
}