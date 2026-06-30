package com.example.dogcatsquare.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dogcatsquare.R
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
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.main_frm,
                    TermsFragment.newInstance(
                        "서비스 이용약관",
                        TermsFragment.TYPE_SERVICE
                    )
                )
                .addToBackStack(null)
                .commit()
        }

        // 개인정보 처리방침
        binding.privacyPolicyLayout.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.main_frm,
                    TermsFragment.newInstance(
                        "개인정보 처리방침",
                        TermsFragment.TYPE_PRIVACY
                    )
                )
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}