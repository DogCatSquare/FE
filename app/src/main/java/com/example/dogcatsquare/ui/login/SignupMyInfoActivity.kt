package com.example.dogcatsquare.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dogcatsquare.MainActivity
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.region.Region
import com.example.dogcatsquare.data.region.RegionData
import com.example.dogcatsquare.data.region.SubRegion
import com.example.dogcatsquare.databinding.ActivitySignupMyInfoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import java.util.Calendar

class SignupMyInfoActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignupMyInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupMyInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 지역 설정
        binding.selectLocBtn.setOnClickListener {
            showRegionBottomSheet { selectedRegion ->
                binding.selectLocBtn.text = selectedRegion
            }
        }

        // 캘린더 설정
        binding.selectBuyDateBtn.setOnClickListener { // 원하는 버튼 ID로 변경
            showCustomCalendarBottomSheet { selectedDate ->
                Toast.makeText(this, "선택한 날짜: $selectedDate", Toast.LENGTH_SHORT).show()
            }
        }

        // 구매 주기 설정
        setWeek()

        // 완료 버튼 클릭 시 메인 페이지로 넘어가기
        binding.signupBtn.setOnClickListener {
            chechMyInfo()
        }
    }

    // 내 정보 확인 후 메인 화면으로 넘어가기
    private fun chechMyInfo() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    // 지역 선택 바텀 시트
    private fun showRegionBottomSheet(onRegionSelected: (String) -> Unit) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_pick_loc, null)
        bottomSheetDialog.setContentView(view)

        val firstColumn: ListView = view.findViewById(R.id.region1_column)
        val secondColumn: ListView = view.findViewById(R.id.region2_column)
        val thirdColumn: ListView = view.findViewById(R.id.region3_column)
        val confirmBtn: Button = view.findViewById(R.id.confirm_button)

        val regions = RegionData.regions

        // 선택된 항목 변수 초기화
        var selectedFirst: Region? = regions.firstOrNull() // 기본값: 첫 번째 지역 (서울)
        var selectedSecond: SubRegion? = selectedFirst?.subRegions?.firstOrNull()
        var selectedThird: String? = selectedSecond?.districts?.firstOrNull()

        // 어댑터 초기화
        val firstAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, regions.map { it.name })
        val secondAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())
        val thirdAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())

        // 첫 번째 컬럼 설정
        firstColumn.adapter = firstAdapter
        firstColumn.setOnItemClickListener { _, _, position, _ ->
            selectedFirst = regions[position]
            val secondData = selectedFirst?.subRegions?.map { it.name } ?: emptyList()

            // 두 번째 컬럼 업데이트
            secondAdapter.clear()
            secondAdapter.addAll(secondData)
            secondAdapter.notifyDataSetChanged()

            // 세 번째 컬럼 초기화
            selectedSecond = null
            selectedThird = null
            thirdAdapter.clear()
            thirdAdapter.notifyDataSetChanged()
        }

        // 두 번째 컬럼 설정
        secondColumn.adapter = secondAdapter
        secondColumn.setOnItemClickListener { _, _, position, _ ->
            selectedSecond = selectedFirst?.subRegions?.get(position)
            val thirdData = selectedSecond?.districts ?: emptyList()

            // 세 번째 컬럼 업데이트
            thirdAdapter.clear()
            thirdAdapter.addAll(thirdData)
            thirdAdapter.notifyDataSetChanged()

            // 세 번째 컬럼 선택 초기화
            selectedThird = null
        }

        // 세 번째 컬럼 설정
        thirdColumn.adapter = thirdAdapter
        thirdColumn.setOnItemClickListener { _, _, position, _ ->
            selectedThird = thirdAdapter.getItem(position)
        }

        // 기본 데이터 초기화
        selectedFirst?.let {
            val secondData = it.subRegions.map { subRegion -> subRegion.name }
            secondAdapter.clear()
            secondAdapter.addAll(secondData)
            secondAdapter.notifyDataSetChanged()

            val thirdData = selectedSecond?.districts ?: emptyList()
            thirdAdapter.clear()
            thirdAdapter.addAll(thirdData)
            thirdAdapter.notifyDataSetChanged()
        }

        // 완료 버튼 클릭
        confirmBtn.setOnClickListener {
            val selectedRegion = "${selectedFirst?.name ?: ""} ${selectedSecond?.name ?: ""} ${selectedThird ?: ""}"
            onRegionSelected(selectedRegion)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }


    // 캘린더 뷰 바텀시트
    private fun showCustomCalendarBottomSheet(onDateSelected: (String) -> Unit) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_pick_buy_date, null)
        bottomSheetDialog.setContentView(view)

        val yearButton: MaterialButton = view.findViewById(R.id.year_button)
        val monthButton: MaterialButton = view.findViewById(R.id.month_button)
        val calendarGrid: GridLayout = view.findViewById(R.id.calendar_grid)
        val confirmButton: Button = view.findViewById(R.id.confirm_button)

        val calendar = Calendar.getInstance()
        var selectedYear = calendar.get(Calendar.YEAR)
        var selectedMonth = calendar.get(Calendar.MONTH) // 0부터 시작

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
                val emptyView = TextView(this)
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
                val dateView = TextView(this)
                dateView.text = day.toString()
                dateView.textSize = 16f
                dateView.gravity = android.view.Gravity.CENTER
                dateView.setPadding(8, 8, 8, 8)
                dateView.setBackgroundResource(android.R.drawable.dialog_holo_light_frame)

                dateView.layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }

                dateView.setOnClickListener {
                    Toast.makeText(this, "${year}년 ${month + 1}월 ${day}일 선택됨", Toast.LENGTH_SHORT).show()
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
            val selectedDate = "${selectedYear}년 ${selectedMonth + 1}월 ${calendar.get(Calendar.DAY_OF_MONTH)}일"
            onDateSelected(selectedDate)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun showDropdownDialog(title: String, items: Array<String>, onItemSelected: (String) -> Unit) {
        val builder = android.app.AlertDialog.Builder(this)
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
            } else {
                Toast.makeText(this, "최소 1주까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 증가 버튼 클릭
        binding.increaseBtn.setOnClickListener {
            if (count < 10) { // 최대값 설정
                count++
                binding.countText.text = "${count}주"
            } else {
                Toast.makeText(this, "최대 10주까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}