package com.example.dogcatsquare.ui.home

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.api.DDayRetrofitItf
import com.example.dogcatsquare.data.model.home.AddDDayRequest
import com.example.dogcatsquare.data.model.home.AddDDayResponse
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.FragmentAddDDayBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Objects

class AddDDayFragment : Fragment() {
    lateinit var binding: FragmentAddDDayBinding

    var ddayCount = 3

    private var date: String = ""
    private var during: Int = 1

    private fun getToken(): String? {
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddDDayBinding.inflate(inflater, container, false)

        // 상단바 색깔
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)

        // 배경화면 클릭 시 키보드 숨기기
        binding.addDDayFragment.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN){
                requireActivity().currentFocus?.let { view ->
                    val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    view.clearFocus()
                }
            }
            false // 터치 이벤트를 소비하지 않음
        }

        // 디데이 이름 글자 수 감지
        val dayNameET = binding.dayNameEt
        val charCountTV = binding.charCountTv

        // EditText에 TextWatcher 추가
        dayNameET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0
                charCountTV.text = "$currentLength/7"
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 캘린더 설정
        binding.daySelectBtn.setOnClickListener { // 원하는 버튼 ID로 변경
            showCustomCalendarBottomSheet { selectedDate ->
                Toast.makeText(requireContext(), "선택한 날짜: $selectedDate", Toast.LENGTH_SHORT).show()
                binding.daySelectBtn.text = selectedDate
                date = selectedDate
            }
        }

        // 구매 주기 설정
        setDayWeek()

        binding.setDayDoneBtn.setOnClickListener {
            val title = binding.dayNameEt.text.toString()
            addDDay(title, date, during)
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

                if (selectedDate.before(today)) {
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
                                child.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                                child.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray3))
                            }
                        }

                        dateView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.main_color1))
                        dateView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
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

    private fun setDayWeek() {
        var count = 1 // 초기 값 설정

        // 감소 버튼 클릭
        binding.dayDecreaseBtn.setOnClickListener {
            if (count > 1) { // 최소값 설정
                count--
                binding.dayCountText.text = "${count}주"
            }
        }

        // 증가 버튼 클릭
        binding.dayIncreaseBtn.setOnClickListener {
            if (count < 12) { // 최대값 설정
                count++
                binding.dayCountText.text = "${count}주"
            }
        }

        during = count
    }

    private fun addDDay(title: String, day: String, term: Int) {
        val token = getToken()

        val addDDayService = RetrofitObj.getRetrofit(requireContext()).create(DDayRetrofitItf::class.java)
        addDDayService.addDDay("Bearer $token", AddDDayRequest(title, day, term)).enqueue(object : Callback<AddDDayResponse> {
            override fun onResponse(call: Call<AddDDayResponse>, response: Response<AddDDayResponse>) {
                Log.d("AddDDay/Response", response.toString())

                if (response.isSuccessful) {
                    val resp = response.body()
                    resp?.let { resp ->
                        if (resp.isSuccess) {
                            ddayCount++
                            parentFragmentManager.popBackStack()
                        } else {
                            Log.e("AddDDay/ERROR", "디데이 불러오기 실패: ${resp.message}")
                        }
                    }
                } else {
                    Log.e("AddDDay/ERROR", "응답 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<AddDDayResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())            }

        })
    }
}