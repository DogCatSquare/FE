package com.example.dogcatsquare.ui.map

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.ActivityMapwalkingTypeBinding

class WalkingReviewTypeActivity : AppCompatActivity() {
    lateinit var binding : ActivityMapwalkingTypeBinding

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityMapwalkingTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    // 버튼 클릭 시 호출될 함수
    fun onDifficultyButtonClick(view: View) {
        // 셋 중 하나만 활성화하도록 하기
        resetButtonColors()

        // 클릭된 버튼에 색상 적용
        when (view.id) {
            R.id.easy_bt -> {
                binding.easyBt.setBackgroundColor(Color.parseColor("#F4FCF5"))  // 색상 변경
                binding.easyBt.setTextColor(Color.parseColor("#3E7C43"))  // 텍스트 색상 변경
            }
            R.id.normal_bt -> {
                binding.normalBt.setBackgroundColor(Color.parseColor("#F4FCF5"))
                binding.normalBt.setTextColor(Color.parseColor("#3E7C43"))
            }
            R.id.difficulty_bt -> {
                binding.difficultyBt.setBackgroundColor(Color.parseColor("#F4FCF5"))
                binding.difficultyBt.setTextColor(Color.parseColor("#3E7C43"))
            }
        }

        // 난이도 버튼 상태 확인 후 Completion 버튼 색 변경
        updateCompletionButtonState()
    }

    //특이사항
    private fun resetButtonColors() {
        binding.easyBt.setBackgroundColor(Color.parseColor("#F6F6F6"))
        binding.normalBt.setBackgroundColor(Color.parseColor("#F6F6F6"))
        binding.difficultyBt.setBackgroundColor(Color.parseColor("#F6F6F6"))

        binding.easyBt.setTextColor(Color.parseColor("#9E9E9E"))
        binding.normalBt.setTextColor(Color.parseColor("#9E9E9E"))
        binding.difficultyBt.setTextColor(Color.parseColor("#9E9E9E"))
    }

    fun onButtonClick(view: View) {
        // 클릭된 버튼이 이미 활성화된 상태인 경우 원래 색상으로 돌아가게 하기
        when (view.id) {
            R.id.Washroom_bt -> {
                toggleButtonState(binding.WashroomBt)
            }
            R.id.parking_bt -> {
                toggleButtonState(binding.parkingBt)
            }
            R.id.wastebasket_bt -> {
                toggleButtonState(binding.wastebasketBt)
            }
            R.id.stairs_bt -> {
                toggleButtonState(binding.stairsBt)
            }
            R.id.drinking_bt -> {
                toggleButtonState(binding.drinkingBt)
            }
            R.id.writing_bt -> {
                toggleButtonState(binding.writingBt)
            }
        }
    }

    private fun toggleButtonState(button: Button) {
        if (button.currentTextColor == Color.parseColor("#3E7C43")) {
            // 이미 활성화된 버튼이면 색상 초기화
            button.setBackgroundColor(Color.parseColor("#F6F6F6"))
            button.setTextColor(Color.parseColor("#9E9E9E"))
        } else {
            // 클릭된 버튼을 활성화 상태로 변경
            button.setBackgroundColor(Color.parseColor("#F4FCF5"))
            button.setTextColor(Color.parseColor("#3E7C43"))
        }
    }

    // 난이도 버튼 상태 확인 후 Completion 버튼 상태 업데이트
    private fun updateCompletionButtonState() {
        if (isAnyDifficultyButtonSelected()) {
            binding.CompletionBt.setBackgroundColor(Color.parseColor("#FFB200")) // 활성화 색상
            binding.CompletionBt.setTextColor(Color.WHITE) // 텍스트 색상
        } else {
            binding.CompletionBt.setBackgroundColor(Color.parseColor("#F6F6F6")) // 비활성화 색상
            binding.CompletionBt.setTextColor(Color.parseColor("#9E9E9E")) // 텍스트 색상
        }
    }

    // 난이도 버튼 중 하나라도 활성화되어 있는지 확인
    private fun isAnyDifficultyButtonSelected(): Boolean {
        return binding.easyBt.currentTextColor == Color.parseColor("#3E7C43") ||
                binding.normalBt.currentTextColor == Color.parseColor("#3E7C43") ||
                binding.difficultyBt.currentTextColor == Color.parseColor("#3E7C43")
    }


}