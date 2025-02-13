package com.example.dogcatsquare.ui.mypage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.data.model.mypage.MyReview
import com.example.dogcatsquare.databinding.FragmentMyReviewBinding

class MyReviewFragment : Fragment() {
    lateinit var binding: FragmentMyReviewBinding

    private var myReviewDatas = ArrayList<MyReview>()
    var currentPage = 1 // 현재 페이지 번호를 관리하는 변수

    private fun getToken(): String?{
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyReviewBinding.inflate(inflater, container, false)

        setupMyReviewRecyclerView()

        return binding.root
    }

    private fun setupMyReviewRecyclerView() {
        myReviewDatas.clear()
        myReviewDatas.apply {
            add(MyReview("가나다 동물병원", "리뷰내용", "2025-01-10", 101, null))
            add(MyReview("가나다 동물병원", "리뷰내용", "2025-01-10", 101, null))
            add(MyReview("가나다 동물병원", "리뷰내용", "2025-01-10", 101, null))
            add(MyReview("가나다 동물병원", "리뷰내용", "2025-01-10", 101, null))
            add(MyReview("가나다 동물병원", "리뷰내용", "2025-01-10", 101, null))
            add(MyReview("가나다 동물병원", "리뷰내용", "2025-01-10", 101, null))
        }

        val myReviewRVAdapter = MyReviewRVAdapter(myReviewDatas)
        binding.myReviewRv.adapter = myReviewRVAdapter
        binding.myReviewRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    private fun getMyReview(page: Int) {
        val token = getToken()

    }
}