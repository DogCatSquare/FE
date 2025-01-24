package com.example.dogcatsquare.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.home.Event
import com.example.dogcatsquare.databinding.FragmentEventBinding

class EventFragment: Fragment() {
    lateinit var binding: FragmentEventBinding

    private var eventDatas = ArrayList<Event>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventBinding.inflate(inflater, container, false)

        setupEventRecyclerView()

        return binding.root
    }

    private fun setupEventRecyclerView() {
        // 데이터 초기화
        eventDatas.clear()

        // 핫플 임시 더미 데이터
        eventDatas.apply {
            add(Event("2025 케이펫페어 수원 시즌1", "2025.02.21 ~ 2025.02.23", R.drawable.img_event1))
            add(Event("2025 케이펫페어 수원 시즌1", "2025.02.21 ~ 2025.02.23", R.drawable.img_event2))
        }

        // hot place recycler view
        val eventRVAdapter = EventRVAdapter(eventDatas)
        binding.eventRv.adapter = eventRVAdapter
        binding.eventRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        // 클릭 인터페이스
        eventRVAdapter.setMyItemClickListener(object : EventRVAdapter.OnItemClickListener {
            override fun onItemClick(event: Event) {
                // Fragment 전환
//                requireActivity().supportFragmentManager.beginTransaction()
//                    .replace(R.id.main_frm, fragment)
//                    .addToBackStack(null)
//                    .commitAllowingStateLoss()
            }
        })
    }
}