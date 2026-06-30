package com.example.dogcatsquare.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dogcatsquare.databinding.FragmentHomeSettingBinding

class HomeSettingFragment : Fragment() {

    private var _binding: FragmentHomeSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeSettingBinding.inflate(inflater, container, false)

        initClickListener()

        return binding.root
    }

    private fun initClickListener() {

        // 뒤로가기
        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 서비스 이용약관
        binding.serviceTermLayout.setOnClickListener {
            openUrl("서비스 이용약관 URL")
        }

        // 개인정보 처리방침
        binding.privacyPolicyLayout.setOnClickListener {
            openUrl("개인정보 처리방침 URL")
        }
    }

    private fun openUrl(url: String) {
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}