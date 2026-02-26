package com.example.dogcatsquare.ui.map.walking

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentMapwalkingReviewtypeBinding
import com.example.dogcatsquare.ui.map.location.MapFragment

class WalkingReviewTypeFragment : Fragment() {

    private var _binding: FragmentMapwalkingReviewtypeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapwalkingReviewtypeBinding.inflate(inflater, container, false)

        binding.CompletionBt.setOnClickListener {
            val nextFragment = MapFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, nextFragment)
                .commit()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 뒤로가기 버튼 설정
        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 2. 난이도 버튼 클릭 리스너 설정
        binding.easyBt.setOnClickListener { onDifficultyButtonClick(it) }
        binding.normalBt.setOnClickListener { onDifficultyButtonClick(it) }
        binding.difficultyBt.setOnClickListener { onDifficultyButtonClick(it) }

        // 3. 기타 버튼 클릭 리스너 설정 (직접 작성 버튼 포함)
        binding.WashroomBt.setOnClickListener { onButtonClick(it) }
        binding.parkingBt.setOnClickListener { onButtonClick(it) }
        binding.wastebasketBt.setOnClickListener { onButtonClick(it) }
        binding.stairsBt.setOnClickListener { onButtonClick(it) }
        binding.drinkingBt.setOnClickListener { onButtonClick(it) }
        binding.manualInputBt.setOnClickListener { onButtonClick(it) } // 추가된 직접 작성 버튼
    }

    private fun onDifficultyButtonClick(view: View) {
        resetButtonColors()

        // 클릭된 버튼 활성화 (둥근 모양 유지를 위해 backgroundTintList 사용)
        val selectedButton = view as Button
        selectedButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F4FCF5"))
        selectedButton.setTextColor(Color.parseColor("#3E7C43"))

        updateCompletionButtonState()
    }

    private fun resetButtonColors() {
        val buttons = listOf(binding.easyBt, binding.normalBt, binding.difficultyBt)
        for (button in buttons) {
            button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F6F6F6"))
            button.setTextColor(Color.parseColor("#9E9E9E"))
        }
    }

    private fun onButtonClick(view: View) {
        toggleButtonState(view as Button)
    }

    private fun toggleButtonState(button: Button) {
        // 현재 텍스트 색상으로 활성화 여부 판단
        if (button.currentTextColor == Color.parseColor("#3E7C43")) {
            button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F6F6F6"))
            button.setTextColor(Color.parseColor("#9E9E9E"))
        } else {
            button.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F4FCF5"))
            button.setTextColor(Color.parseColor("#3E7C43"))
        }
    }

    private fun updateCompletionButtonState() {
        if (isAnyDifficultyButtonSelected()) {
            binding.CompletionBt.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFB200"))
            binding.CompletionBt.setTextColor(Color.WHITE)
            binding.CompletionBt.isEnabled = true
        } else {
            binding.CompletionBt.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F6F6F6"))
            binding.CompletionBt.setTextColor(Color.parseColor("#9E9E9E"))
            binding.CompletionBt.isEnabled = false
        }
    }

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