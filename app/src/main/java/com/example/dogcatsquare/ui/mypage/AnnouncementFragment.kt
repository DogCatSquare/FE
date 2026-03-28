package com.example.dogcatsquare.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.announcement.Notice
import com.example.dogcatsquare.data.model.map.BaseResponse
import com.example.dogcatsquare.data.network.RetrofitClient
import com.example.dogcatsquare.databinding.FragmentAnnouncementBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnnouncementFragment : Fragment() {
    private var _binding: FragmentAnnouncementBinding? = null
    private val binding get() = _binding!!

    private var announcementDatas = ArrayList<Notice>()
    private lateinit var announcementRVAdapter: AnnouncementRVAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAnnouncementBinding.inflate(inflater, container, false)

        setupAnnouncementRV()
        fetchNotices() // API 호출로 변경
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

        announcementRVAdapter.setMyItemClickListener(object : AnnouncementRVAdapter.OnItemClickListener {
            override fun onItemClick(announcement: Notice) {
                val detailFragment = AnnouncementDetailFragment.newInstance(announcement.id)
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, detailFragment)
                    .addToBackStack(null)
                    .commit()
            }
        })
    }

    private fun fetchNotices() {
        RetrofitClient.noticeApiService.getNotices().enqueue(object : Callback<BaseResponse<List<Notice>>> {
            override fun onResponse(
                call: Call<BaseResponse<List<Notice>>>,
                response: Response<BaseResponse<List<Notice>>>
            ) {
                if (response.isSuccessful) {
                    val baseResponse = response.body()
                    if (baseResponse?.isSuccess == true) {
                        announcementDatas.clear()
                        baseResponse.result?.let { notices ->
                            announcementDatas.addAll(notices)
                        }
                        announcementRVAdapter.notifyDataSetChanged()
                        updateVisibility()
                    }
                }
            }

            override fun onFailure(call: Call<BaseResponse<List<Notice>>>, t: Throwable) {
                Log.e("AnnouncementFragment", "Failed to fetch notices", t)
            }
        })
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