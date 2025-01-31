package com.example.dogcatsquare.ui.map.location

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentMapReportBinding

class MapReportFragment : Fragment() {
    private var _binding: FragmentMapReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton()
        setupRadioGroup()
        setupOtherReasonEditText()
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupRadioGroup() {
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ),
            intArrayOf(
                Color.GRAY,
                Color.parseColor("#FFB200")
            )
        )

        // 라디오 버튼들에 색상 적용
        binding.apply {
            rgReasons.apply {
                rbAdvertising.buttonTintList = colorStateList
                rbHateSpeech.buttonTintList = colorStateList
                rbObsceneContent.buttonTintList = colorStateList
                rbSpam.buttonTintList = colorStateList
                rbPersonalInfo.buttonTintList = colorStateList
                rbOther.buttonTintList = colorStateList
            }
        }

        // CustomEditText 초기 상태 설정 (처음에는 비활성화)
        binding.etOtherReason.isEnabled = false
        binding.etOtherReason.alpha = 0.5f

        binding.rgReasons.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbOther -> {
                    binding.etOtherReason.isEnabled = true
                    binding.etOtherReason.alpha = 1.0f
                    binding.reportButton.setImageResource(R.drawable.bt_deactivated_complete)
                }
                -1 -> {
                    binding.etOtherReason.isEnabled = false
                    binding.etOtherReason.alpha = 0.5f
                    binding.reportButton.setImageResource(R.drawable.bt_deactivated_complete)
                }
                else -> {
                    // 다른 라디오 버튼 선택 시
                    binding.etOtherReason.isEnabled = false
                    binding.etOtherReason.alpha = 0.5f
                    binding.etOtherReason.setText("")
                    binding.reportButton.setImageResource(R.drawable.bt_activated_complete)
                }
            }
        }
    }

    private fun setupOtherReasonEditText() {
        binding.etOtherReason.addTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.rgReasons.checkedRadioButtonId == R.id.rbOther) {
                    val length = s?.length ?: 0
                    binding.reportButton.setImageResource(
                        if (length >= 10) R.drawable.bt_activated_complete
                        else R.drawable.bt_deactivated_complete
                    )
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}