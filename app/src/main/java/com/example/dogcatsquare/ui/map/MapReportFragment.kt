package com.example.dogcatsquare.ui.map

import android.os.Bundle
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
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupRadioGroup() {
        binding.rgReasons.setOnCheckedChangeListener { group, checkedId ->
            // "기타" RadioButton이 선택되었는지 확인
            binding.etOtherReason.visibility = if (checkedId == R.id.rbOther) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}