package com.example.dogcatsquare.ui.map.location

import android.app.Dialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.DialogSortBinding
import com.example.dogcatsquare.ui.wish.WishPlaceFragment
import com.example.dogcatsquare.ui.wish.WishWalkFragment

class SortDialogFragment : DialogFragment() {
    private var _binding: DialogSortBinding? = null
    private val binding get() = _binding!!

    private val selectedColor = Color.parseColor("#FFB200")
    private val unselectedColor = Color.parseColor("#9E9E9E")

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSortBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 현재 정렬 상태 가져오기
        val currentSortType = arguments?.getString("currentSortType") ?: "주소기준"
        // 초기 상태 설정
        updateSelectionState(isAddressSelected = currentSortType == "주소기준")

        binding.exitButton.setOnClickListener {
            dismiss()
        }

        binding.addressText.setOnClickListener {
            // 선택 상태 즉시 업데이트
            binding.addressImg.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN)
            binding.addressText.setTextColor(selectedColor)
            binding.locationImg.setColorFilter(unselectedColor, PorterDuff.Mode.SRC_IN)
            binding.locationText.setTextColor(unselectedColor)

            when (val parentFrag = parentFragment) {
                is MapFragment -> parentFrag.updateSortText("주소기준")
                is WishPlaceFragment -> parentFrag.updateSortText("주소기준")
                is WishWalkFragment -> parentFrag.updateSortText("주소기준")
            }
            dismiss()
        }

        binding.locationText.setOnClickListener {
            // 선택 상태 즉시 업데이트
            binding.locationImg.setColorFilter(selectedColor, PorterDuff.Mode.SRC_IN)
            binding.locationText.setTextColor(selectedColor)
            binding.addressImg.setColorFilter(unselectedColor, PorterDuff.Mode.SRC_IN)
            binding.addressText.setTextColor(unselectedColor)

            when (val parentFrag = parentFragment) {
                is MapFragment -> parentFrag.updateSortText("위치기준")
                is WishPlaceFragment -> parentFrag.updateSortText("위치기준")
                is WishWalkFragment -> parentFrag.updateSortText("위치기준")
            }
            dismiss()
        }
    }

    private fun updateSelectionState(isAddressSelected: Boolean) {
        // 주소 기준 아이템 색상 변경
        binding.addressImg.setColorFilter(
            if (isAddressSelected) selectedColor else unselectedColor,
            PorterDuff.Mode.SRC_IN
        )
        binding.addressText.setTextColor(
            if (isAddressSelected) selectedColor else unselectedColor
        )

        // 현재 위치 아이템 색상 변경
        binding.locationImg.setColorFilter(
            if (!isAddressSelected) selectedColor else unselectedColor,
            PorterDuff.Mode.SRC_IN
        )
        binding.locationText.setTextColor(
            if (!isAddressSelected) selectedColor else unselectedColor
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}