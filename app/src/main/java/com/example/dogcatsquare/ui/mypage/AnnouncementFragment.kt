package com.example.dogcatsquare.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.data.model.announcement.AnnouncementResponse
import com.example.dogcatsquare.databinding.FragmentAnnouncementBinding

class AnnouncementFragment : Fragment() {
    private var _binding: FragmentAnnouncementBinding? = null
    private val binding get() = _binding!!

    private var announcementDatas = ArrayList<AnnouncementResponse>()
    private lateinit var announcementRVAdapter: AnnouncementRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAnnouncementBinding.inflate(inflater, container, false)

        setupAnnouncementRV() // 1. 리사이클러뷰 먼저 설정
        setDummyData()        // 2. 그 다음 데이터 채우기
        updateVisibility()

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    private fun setupAnnouncementRV() {
        announcementRVAdapter = AnnouncementRVAdapter(announcementDatas)

        binding.announcementRv.adapter = announcementRVAdapter
        binding.announcementRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        // 공지사항 api 연결
    }

    // 공지사항 더미데이터
    private fun setDummyData() {
        announcementDatas.apply {
            add(AnnouncementResponse(1, "공지", "서비스 이용약관 개정 안내", "2026.03.07"))
            add(AnnouncementResponse(2, "업데이트", "v2.1.0 버전 업데이트 배포 완료", "2026.03.05"))
            add(AnnouncementResponse(3, "이벤트", "봄맞이 산책 인증 이벤트 당첨자 발표", "2026.03.01"))
            add(AnnouncementResponse(4, "공지", "시스템 정기 점검 안내 (03/10)", "2026.02.28"))
            add(AnnouncementResponse(5, "업데이트", "강아지/고양이 등록 프로세스 개선 안내", "2026.02.25"))
            add(AnnouncementResponse(6, "이벤트", "친구 초대하고 포인트 받아가세요!", "2026.02.20"))
        }

        // 데이터가 추가된 후 어댑터에 알림 (어댑터 변수명이 announcementAdapter인 경우)
        announcementRVAdapter.notifyDataSetChanged()
    }

    private fun updateVisibility() {
        if (announcementDatas.isEmpty()) {
            binding.noneIv.visibility = View.VISIBLE
            binding.noneTv.visibility = View.VISIBLE
            binding.announcementRv.visibility = View.GONE
        } else {
            binding.noneIv.visibility = View.GONE
            binding.noneTv.visibility = View.GONE
            binding.announcementRv.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}