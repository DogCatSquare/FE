package com.example.dogcatsquare.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentMypageBinding

class MypageFragment : Fragment() {
    lateinit var binding: FragmentMypageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMypageBinding.inflate(inflater, container, false)

        // 내 정보 수정
        binding.goEditInfoIv.setOnClickListener {
            // Fragment 전환
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, EditInfoFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        // 내 커뮤니티 모아보기
        binding.goMyCommunityIv.setOnClickListener {
            // Fragment 전환
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MyCommunityFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        // 내 후기 모아보기
        binding.goMyReviewIv.setOnClickListener {
            // Fragment 전환
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MyReviewFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        // 공지사항
        binding.goAnnouncementIv.setOnClickListener {

        }

        // 회원탈퇴 클릭
        binding.deleteTv.setOnClickListener {
            showDeleteDialog()
        }

        // 로그아웃 버튼 클릭
        binding.logoutBtn.setOnClickListener {

        }

        return binding.root
    }

    private fun showDeleteDialog() {
        val dialog = CustomDeleteDialog(requireContext())

        dialog.setItemClickListener(object : CustomDeleteDialog.ItemClickListener{
            override fun onClick(message: String) {
                Toast.makeText(requireContext(), "${message}", Toast.LENGTH_SHORT).show()
            }
        })

        dialog.show()
    }
}