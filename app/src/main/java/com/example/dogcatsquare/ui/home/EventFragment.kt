package com.example.dogcatsquare.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.api.EventRetrofitItf
import com.example.dogcatsquare.data.model.home.Event
import com.example.dogcatsquare.data.model.home.GetAllEventsResponse
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.FragmentEventBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    private fun setupEventRecyclerView() {
        // 데이터 초기화
        eventDatas.clear()

        // hot place recycler view
        val eventRVAdapter = EventRVAdapter(eventDatas)
        binding.eventRv.adapter = eventRVAdapter
        binding.eventRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        getAllEvents(eventRVAdapter)

        // 클릭 인터페이스
        eventRVAdapter.setMyItemClickListener(object : EventRVAdapter.OnItemClickListener {
            override fun onItemClick(event: Event) {
                // event 연결
                val uri = Uri.parse(event.eventUrl);
                val it = Intent(Intent.ACTION_VIEW, uri);
                startActivity(it)
            }
        })
    }

    private fun getAllEvents(adapter: EventRVAdapter) {
        val getAllEventsService = RetrofitObj.getRetrofit().create(EventRetrofitItf::class.java)
        getAllEventsService.getAllEvents().enqueue(object: Callback<GetAllEventsResponse> {
            override fun onResponse(call: Call<GetAllEventsResponse>, response: Response<GetAllEventsResponse>) {
                Log.d("GetEvent/SUCCESS", response.toString())
                val resp: GetAllEventsResponse = response.body()!!

                if (resp != null) {
                    if (resp.isSuccess) {
                        Log.d("GetEvent", "디데이 전체 조회 성공")

                        val events = resp.result.map { event ->
                            Event (
                                id = event.id,
                                title = event.title,
                                period = event.period,
                                bannerImageUrl = event.bannerImageUrl,
                                eventUrl = event.eventUrl
                            )
                        }.toList()

                        eventDatas.addAll(events)
                        Log.d("EventList", eventDatas.toString())
                        adapter.notifyDataSetChanged()
                    }

                } else {
                    Log.e("GetEvent/ERROR", "응답 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<GetAllEventsResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }
        })
    }
}