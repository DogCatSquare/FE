package com.example.dogcatsquare.ui.login

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.DatePicker
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.ActivitySignupPetInfoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class SignupPetInfoActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignupPetInfoBinding

    // 초기 반려동물 선택 상태
    var selectedAnimal: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupPetInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 다음 버튼 누르면 넘어가기
        binding.signupPetNextBtn.setOnClickListener {
            checkPetInfo()
        }

        // 생일 입력 버튼
        binding.birthSelectBtn.setOnClickListener {
            showBottomSheetDialog()
        }

        // 강아지, 고양이 선택 버튼
        binding.dogSelectBtn.setOnClickListener {
            selectedAnimal = "강아지"

            // 버튼 스타일 업데이트
            binding.dogSelectBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.orange))
            binding.catSelectBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.light_orange))

            Toast.makeText(this, "${selectedAnimal}", Toast.LENGTH_SHORT).show()
        }

        binding.catSelectBtn.setOnClickListener {
            selectedAnimal = "고양이"

            // 버튼 스타일 업데이트
            binding.dogSelectBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.light_orange))
            binding.catSelectBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.orange))

            Toast.makeText(this, "${selectedAnimal}", Toast.LENGTH_SHORT).show()
        }
    }

    // 날짜 선택 바텀시트
    private fun showBottomSheetDialog() {
        // 바텀시트 다이얼로그 생성
        val bottomSheetDialog = BottomSheetDialog(this)

        // 레이아웃 인플레이트
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_pick_date, null)
        bottomSheetDialog.setContentView(view)

        // NumberPicker 및 버튼 초기화
        val yearPicker = view.findViewById<NumberPicker>(R.id.year_picker)
        val monthPicker = view.findViewById<NumberPicker>(R.id.month_picker)
        val dayPicker = view.findViewById<NumberPicker>(R.id.day_picker)
        val confirmButton = view.findViewById<Button>(R.id.confirm_button)

        // NumberPicker 범위 설정
        yearPicker.minValue = 1900
        yearPicker.maxValue = 2100
        yearPicker.value = 2023

        monthPicker.minValue = 1
        monthPicker.maxValue = 12

        dayPicker.minValue = 1
        dayPicker.maxValue = 31

        // 완료 버튼 클릭 이벤트
        confirmButton.setOnClickListener {
            val year = yearPicker.value
            val month = monthPicker.value
            val day = dayPicker.value

            val selectedDate = "${year}년 ${month}월 ${day}일"
            Toast.makeText(this, "선택된 날짜: $selectedDate", Toast.LENGTH_SHORT).show()

            bottomSheetDialog.dismiss() // 바텀시트 닫기
        }

        // 바텀시트 다이얼로그 표시
        bottomSheetDialog.show()
    }

    // 반려동물 정보 확인하고 넘어가기 -> 추후 수정
    private fun checkPetInfo() {
        val intent = Intent(this, SignupMyInfoActivity::class.java)
        startActivity(intent)
    }
}