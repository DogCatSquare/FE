package com.example.dogcatsquare.ui.mypage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dogcatsquare.data.model.announcement.Notice
import com.example.dogcatsquare.data.model.map.BaseResponse
import com.example.dogcatsquare.data.network.RetrofitClient
import com.example.dogcatsquare.databinding.FragmentAnnouncementDetailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class AnnouncementDetailFragment : Fragment() {
    private var _binding: FragmentAnnouncementDetailBinding? = null
    private val binding get() = _binding!!
    private var noticeId: Long = -1

    companion object {
        fun newInstance(noticeId: Long): AnnouncementDetailFragment {
            val fragment = AnnouncementDetailFragment()
            val args = Bundle()
            args.putLong("noticeId", noticeId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            noticeId = it.getLong("noticeId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnnouncementDetailBinding.inflate(inflater, container, false)

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        if (noticeId != -1L) {
            fetchNoticeDetail(noticeId)
        }

        return binding.root
    }

    private fun fetchNoticeDetail(id: Long) {
        RetrofitClient.noticeApiService.getNoticeDetail(id).enqueue(object : Callback<BaseResponse<Notice>> {
            override fun onResponse(call: Call<BaseResponse<Notice>>, response: Response<BaseResponse<Notice>>) {
                if (response.isSuccessful) {
                    val baseResponse = response.body()
                    if (baseResponse?.isSuccess == true) {
                        baseResponse.result?.let { notice ->
                            updateUI(notice)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<BaseResponse<Notice>>, t: Throwable) {
                Log.e("AnnouncementDetail", "Failed to fetch notice detail", t)
            }
        })
    }

    private fun updateUI(notice: Notice) {
        binding.apply {
            titleTv.text = notice.title
            contentTv.text = notice.content
            
            // 날짜 포맷팅 (ISO 8601 -> yyyy. MM. dd)
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("yyyy. MM. dd", Locale.getDefault())
                val date = inputFormat.parse(notice.createdAt)
                dateTv.text = date?.let { outputFormat.format(it) } ?: notice.createdAt
            } catch (e: Exception) {
                dateTv.text = notice.createdAt
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
