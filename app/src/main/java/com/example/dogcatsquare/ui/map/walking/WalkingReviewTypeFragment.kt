package com.example.dogcatsquare.ui.map.walking

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentMapwalkingReviewtypeBinding
import com.example.dogcatsquare.ui.map.location.MapFragment
import com.example.dogcatsquare.ui.map.walking.data.SpecialDto
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkReviewViewModel

class WalkingReviewTypeFragment : Fragment() {

    private var _binding: FragmentMapwalkingReviewtypeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: WalkReviewViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapwalkingReviewtypeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[WalkReviewViewModel::class.java]

        val placeName = arguments?.getString("placeName") ?: "알 수 없는 장소"
        binding.addressTv.text = placeName

        binding.backBtn.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.easyBt.setOnClickListener { onDifficultyButtonClick(it) }
        binding.normalBt.setOnClickListener { onDifficultyButtonClick(it) }
        binding.difficultyBt.setOnClickListener { onDifficultyButtonClick(it) }

        binding.WashroomBt.setOnClickListener { onButtonClick(it) }
        binding.parkingBt.setOnClickListener { onButtonClick(it) }
        binding.wastebasketBt.setOnClickListener { onButtonClick(it) }
        binding.stairsBt.setOnClickListener { onButtonClick(it) }
        binding.drinkingBt.setOnClickListener { onButtonClick(it) }
        binding.manualInputBt.setOnClickListener { onButtonClick(it) }

        binding.CompletionBt.setOnClickListener {
            val difficulty = getSelectedDifficulty()
            val specialList = getSelectedSpecials()

            viewModel.createWalk(difficulty, specialList)
        }

        viewModel.walkCreateResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccess) {
                Toast.makeText(requireContext(), "산책로 등록 완료!", Toast.LENGTH_SHORT).show()

                parentFragmentManager.popBackStack()
                parentFragmentManager.popBackStack()
                parentFragmentManager.popBackStack()

            } else {
                Toast.makeText(requireContext(), "등록 실패: ${response.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getSelectedDifficulty(): String {
        val activeColor = Color.parseColor("#3E7C43")
        return when {
            binding.easyBt.currentTextColor == activeColor -> "LOW"
            binding.normalBt.currentTextColor == activeColor -> "MIDDLE"
            binding.difficultyBt.currentTextColor == activeColor -> "HIGH"
            else -> "MIDDLE"
        }
    }

    private fun getSelectedSpecials(): List<SpecialDto> {
        val activeColor = Color.parseColor("#3E7C43")
        val specials = mutableListOf<SpecialDto>()

        if (binding.WashroomBt.currentTextColor == activeColor)
            specials.add(SpecialDto(type = "TOILET", customValue = ""))

        if (binding.parkingBt.currentTextColor == activeColor)
            specials.add(SpecialDto(type = "PARKING", customValue = ""))

        if (binding.wastebasketBt.currentTextColor == activeColor)
            specials.add(SpecialDto(type = "WASTEBASKET", customValue = ""))

        if (binding.stairsBt.currentTextColor == activeColor)
            specials.add(SpecialDto(type = "STAIRS", customValue = ""))

        if (binding.drinkingBt.currentTextColor == activeColor)
            specials.add(SpecialDto(type = "WATER", customValue = ""))

        if (binding.manualInputBt.currentTextColor == activeColor) {
            // TODO: 사용자가 직접 입력한 텍스트를 가져와서 customValue에 넣어주세요.
            // 일단은 빈 문자열로 처리하도록 해두었습니다.
            specials.add(SpecialDto(type = "OTHER", customValue = ""))
        }

        return specials
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