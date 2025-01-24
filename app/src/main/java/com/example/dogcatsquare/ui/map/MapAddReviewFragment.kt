package com.example.dogcatsquare.ui.map

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dogcatsquare.databinding.FragmentMapAddReviewBinding

class MapAddReviewFragment : Fragment() {
    private var _binding: FragmentMapAddReviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapAddReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton()
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            // FragmentManager의 BackStack에서 현재 Fragment를 제거하여 이전 화면으로 돌아감
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}