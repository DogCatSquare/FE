package com.example.dogcatsquare.ui.map.walking

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.dogcatsquare.databinding.FragmentMapwalkingReviewtypeBinding

class WalkingReviewTypeFragment : Fragment() {

    private var _binding: FragmentMapwalkingReviewtypeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapwalkingReviewtypeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 난이도 버튼 클릭 리스너 설정
        binding.easyBt.setOnClickListener { onDifficultyButtonClick(it) }
        binding.normalBt.setOnClickListener { onDifficultyButtonClick(it) }
        binding.difficultyBt.setOnClickListener { onDifficultyButtonClick(it) }

        // 기타 버튼 클릭 리스너 설정
        binding.WashroomBt.setOnClickListener { onButtonClick(it) }
        binding.parkingBt.setOnClickListener { onButtonClick(it) }
        binding.wastebasketBt.setOnClickListener { onButtonClick(it) }
        binding.stairsBt.setOnClickListener { onButtonClick(it) }
        binding.drinkingBt.setOnClickListener { onButtonClick(it) }
        binding.writingBt.setOnClickListener { onButtonClick(it) }
    }

    // 버튼 클릭 시 호출될 함수
    private fun onDifficultyButtonClick(view: View) {
        resetButtonColors()

        // 클릭된 버튼에 색상 적용
        when (view.id) {
            binding.easyBt.id -> {
                binding.easyBt.setBackgroundColor(Color.parseColor("#F4FCF5"))
                binding.easyBt.setTextColor(Color.parseColor("#3E7C43"))
            }
            binding.normalBt.id -> {
                binding.normalBt.setBackgroundColor(Color.parseColor("#F4FCF5"))
                binding.normalBt.setTextColor(Color.parseColor("#3E7C43"))
            }
            binding.difficultyBt.id -> {
                binding.difficultyBt.setBackgroundColor(Color.parseColor("#F4FCF5"))
                binding.difficultyBt.setTextColor(Color.parseColor("#3E7C43"))
            }
        }

        // Completion 버튼 색상 업데이트
        updateCompletionButtonState()
    }

    // 버튼 색상 초기화
    private fun resetButtonColors() {
        binding.easyBt.setBackgroundColor(Color.parseColor("#F6F6F6"))
        binding.normalBt.setBackgroundColor(Color.parseColor("#F6F6F6"))
        binding.difficultyBt.setBackgroundColor(Color.parseColor("#F6F6F6"))

        binding.easyBt.setTextColor(Color.parseColor("#9E9E9E"))
        binding.normalBt.setTextColor(Color.parseColor("#9E9E9E"))
        binding.difficultyBt.setTextColor(Color.parseColor("#9E9E9E"))
    }

    // 기타 버튼 클릭 이벤트 처리
    private fun onButtonClick(view: View) {
        when (view.id) {
            binding.WashroomBt.id -> toggleButtonState(binding.WashroomBt)
            binding.parkingBt.id -> toggleButtonState(binding.parkingBt)
            binding.wastebasketBt.id -> toggleButtonState(binding.wastebasketBt)
            binding.stairsBt.id -> toggleButtonState(binding.stairsBt)
            binding.drinkingBt.id -> toggleButtonState(binding.drinkingBt)
            binding.writingBt.id -> toggleButtonState(binding.writingBt)
        }
    }

    // 버튼 상태 토글 (색상 변경)
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
            binding.CompletionBt.setBackgroundColor(Color.parseColor("#FFB200"))
            binding.CompletionBt.setTextColor(Color.WHITE)
        } else {
            binding.CompletionBt.setBackgroundColor(Color.parseColor("#F6F6F6"))
            binding.CompletionBt.setTextColor(Color.parseColor("#9E9E9E"))
        }
    }

    // 난이도 버튼 중 하나라도 활성화되어 있는지 확인
    private fun isAnyDifficultyButtonSelected(): Boolean {
        return binding.easyBt.currentTextColor == Color.parseColor("#3E7C43") ||
                binding.normalBt.currentTextColor == Color.parseColor("#3E7C43") ||
                binding.difficultyBt.currentTextColor == Color.parseColor("#3E7C43")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


