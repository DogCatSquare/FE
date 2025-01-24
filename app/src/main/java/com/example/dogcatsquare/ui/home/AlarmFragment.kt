package com.example.dogcatsquare.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.data.home.Alarm
import com.example.dogcatsquare.databinding.FragmentAlarmBinding

class AlarmFragment : Fragment() {
    lateinit var binding: FragmentAlarmBinding

    private var alarmDatas = ArrayList<Alarm>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlarmBinding.inflate(inflater, container, false)

        setupAlarmRecyclerView()

        return binding.root
    }

    private fun setupAlarmRecyclerView() {
        alarmDatas.clear()
        alarmDatas.apply {
            add(Alarm("동네 이야기", "닉네임님이 [새로운 장난감을 사줬어요]에 댓글을 남겼습니다 지금 바로 확인해보세요", "1분전"))
            add(Alarm("동네 이야기", "닉네임님이 [새로운 장난감을 사줬어요]에 댓글을 남겼습니다 지금 바로 확인해보세요", "1분전"))
            add(Alarm("동네 이야기", "닉네임님이 [새로운 장난감을 사줬어요]에 댓글을 남겼습니다 지금 바로 확인해보세요", "1분전"))
        }

        val alarmRVAdapter = AlarmRVAdapter(alarmDatas)
        binding.alarmRv.adapter = alarmRVAdapter
        binding.alarmRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }
}