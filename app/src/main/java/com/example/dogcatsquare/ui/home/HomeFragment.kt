package com.example.dogcatsquare.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.dogcatsquare.data.map.MapPlace
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.api.DDayRetrofitItf
import com.example.dogcatsquare.data.api.EventRetrofitItf
import com.example.dogcatsquare.data.model.home.DDay
import com.example.dogcatsquare.data.model.home.Event
import com.example.dogcatsquare.data.model.home.GetAllDDayResponse
import com.example.dogcatsquare.data.model.home.GetAllEventsResponse
import com.example.dogcatsquare.data.model.pet.GetAllPetResponse
import com.example.dogcatsquare.data.model.pet.PetList
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.data.post.Pet
import com.example.dogcatsquare.data.post.Post
import com.example.dogcatsquare.databinding.FragmentHomeBinding
import com.example.dogcatsquare.ui.map.location.MapDetailFragment
import com.example.dogcatsquare.ui.map.location.MapEtcFragment
import com.example.dogcatsquare.ui.mypage.HorizontalSpacingItemDecoration
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create
import java.util.Timer
import java.util.TimerTask

class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding

    private val timer = Timer()
    private val handler = Handler(Looper.getMainLooper())

    private var dDayDatas = ArrayList<DDay>()
    private var placeDatas = ArrayList<MapPlace>()
    private var hotPostDatas = ArrayList<Post>()
    private var eventDatas = ArrayList<Event>()

    private fun getToken(): String? {
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // 상단바 색깔
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.light_blue)

        setupWeatherViewPager()
        setupDDayRecyclerView()
        setupHotPlaceRecyclerView()
        setupAdViewPager()
        setupHotPostRecyclerView()
        setupEventRecyclerView()

        binding.homeGoPetEventIv.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, EventFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        return binding.root
    }

    // auto slide
    private fun adAutoSlide(adapter: HomeAdVPAdapter) {
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                handler.post {
                    val nextItem = binding.homeAdVp.currentItem + 1
                    if (nextItem < adapter.itemCount) {
                        binding.homeAdVp.currentItem = nextItem
                    } else {
                        binding.homeAdVp.currentItem = 0 // 순환
                    }
                }
            }
        }, 3000, 3000)
    }

    private fun weatherAutoSlide(adapter: HomeWeatherVPAdapter) {
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                handler.post {
                    val nextItem = binding.homeWeatherVp.currentItem + 1
                    if (nextItem < adapter.itemCount) {
                        binding.homeWeatherVp.currentItem = nextItem
                    } else {
                        binding.homeWeatherVp.currentItem = 0 // 순환
                    }
                }
            }
        }, 4500, 4500)
    }

    // weather viewpager
    private fun setupWeatherViewPager() {
        // ad view pager
        val homeWeatherAdapter = HomeWeatherVPAdapter(this)
        homeWeatherAdapter.addFragment(HomeWeatherFragment())
        homeWeatherAdapter.addFragment(HomeWeatherFragment())

        binding.homeWeatherVp.adapter = homeWeatherAdapter
        binding.homeWeatherVp.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        // 자동 넘기기
        weatherAutoSlide(homeWeatherAdapter)
    }

    // d_day rv
    private fun setupDDayRecyclerView() {
        dDayDatas.clear()

        // d-day recycler view
        val dDayRVAdapter = HomeDDayRVAdapter(dDayDatas)
        binding.homeDdayRv.adapter = dDayRVAdapter
        binding.homeDdayRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        binding.homeDdayRv.addItemDecoration(HorizontalSpacingItemDecoration(15))
        binding.homeDdayRv.setHasFixedSize(true)

        getAllDDay(dDayRVAdapter)

        // 클릭 인터페이스
        dDayRVAdapter.setMyItemClickListener(object: HomeDDayRVAdapter.OnItemClickListener {
            override fun onItemClick(d_day: DDay) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, SetDDayFragment())
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
            }
        })
    }

    private fun getAllDDay(adapter: HomeDDayRVAdapter) {
        val BEARER_TOKEN = getToken()

        val getAllDDayService = RetrofitObj.getRetrofit().create(DDayRetrofitItf::class.java)
        getAllDDayService.getAllDDays("Bearer $BEARER_TOKEN").enqueue(object: Callback<GetAllDDayResponse> {
            override fun onResponse(call: Call<GetAllDDayResponse>, response: Response<GetAllDDayResponse>) {
                Log.d("GetDDay/SUCCESS", response.toString())
                val resp: GetAllDDayResponse = response.body()!!

                if (resp != null) {
                    if (resp.isSuccess) {
                        Log.d("GetDDay", "디데이 전체 조회 성공")

                        val ddays = resp.result.map { dday ->
                            DDay (
                                id = dday.id,
                                title = dday.title,
                                day = dday.day,
                                term  = dday.term,
                                daysLeft = dday.daysLeft,
                                ddayText = dday.ddayText,
                                ddayImageUrl = dday.ddayImageUrl
                            )
                        }.toList()

                        dDayDatas.addAll(ddays)
                        Log.d("DDayList", dDayDatas.toString())
                        adapter.notifyDataSetChanged()
                    }

                } else {
                    Log.e("GetDDay/ERROR", "응답 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<GetAllDDayResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }
        })
    }

    // hot place rv
    private fun setupHotPlaceRecyclerView() {
        // 데이터 초기화
        placeDatas.clear()

        // 핫플 임시 더미 데이터
        placeDatas.apply {
            add(MapPlace(
                id = 0,
                placeName = "가나다 동물병원",
                placeType = "동물병원",
                placeDistance = "0.55km",
                placeLocation = "서울시 성북구 월곡동 77",
                placeCall = "02-1234-5678",
                placeImgUrl = null,
                isOpen = "영업중"
            ))
            add(MapPlace(
                id = 0,
                placeName = "서대문 안산자락길",
                placeType = "산책로",
                placeDistance = "0.55km",
                placeLocation = "서울시 서대문구 봉원사길 75-66 111111111111111111",
                placeCall = "02-1234-5678",
                placeImgUrl = null,
                isOpen = "영업중"
            ))
            add(MapPlace(
                id = 0,
                placeName = "고양이호텔",
                placeType = "호텔",
                placeDistance = "0.55km",
                placeLocation = "서울시 성북구 월곡동 77",
                placeCall = "02-1234-5678",
                placeImgUrl = null,
                isOpen = "영업중"
            ))
            add(MapPlace(
                id = 0,
                placeName = "가나다 동물병원",
                placeType = "동물병원",
                placeDistance = "0.55km",
                placeLocation = "서울시 성북구 월곡동 77",
                placeCall = "02-1234-5678",
                placeImgUrl = null,
                isOpen = "영업중"
            ))
        }

        // hot place recycler view
        val homeHotPlaceRVAdapter = HomeHotPlaceRVAdapter(placeDatas)
        binding.homeHotPlaceRv.adapter = homeHotPlaceRVAdapter
        binding.homeHotPlaceRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // 클릭 인터페이스
        homeHotPlaceRVAdapter.setMyItemClickListener(object : HomeHotPlaceRVAdapter.OnItemClickListener {
            override fun onItemClick(place: MapPlace) {
                // placeType에 따라 다른 Fragment로 전환
                val fragment = if (place.placeType == "동물병원") {
                    MapDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString("placeName", place.placeName)
                            putString("placeType", place.placeType)
                            putString("placeDistance", place.placeDistance)
                            putString("placeLocation", place.placeLocation)
                            putString("placeCall", place.placeCall)
                            putString("placeImgUrl", place.placeImgUrl)
                            putString("isOpen", place.isOpen)
                        }
                    }
                } else {
                    MapEtcFragment().apply {
                        arguments = Bundle().apply {
                            putString("placeName", place.placeName)
                            putString("placeType", place.placeType)
                            putString("placeDistance", place.placeDistance)
                            putString("placeLocation", place.placeLocation)
                            putString("placeCall", place.placeCall)
                            putString("placeImgUrl", place.placeImgUrl)
                            putString("isOpen", place.isOpen)
                        }
                    }
                }

                // Fragment 전환
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
            }
        })
    }

    // ad viewpager
    private fun setupAdViewPager() {
        // ad view pager
        val homeAdAdapter = HomeAdVPAdapter(this)
        homeAdAdapter.addFragment(HomeAdFragment(R.drawable.img_home_ad))
        homeAdAdapter.addFragment(HomeAdFragment(R.drawable.img_home_ad))

        binding.homeAdVp.adapter = homeAdAdapter
        binding.homeAdVp.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        // 자동 넘기기
        adAutoSlide(homeAdAdapter)
    }

    // hot post rv
    private fun setupHotPostRecyclerView() {
        hotPostDatas.clear()

        // 인기 게시물 임시 더미 데이터
        hotPostDatas.apply {
            add(Post(
                1,
                "2025년! 새해 복 많이 받으세요!!",
                "우리 호두도 올해로 5살이 되었어요. 새해 아침부터 터그놀이 하는 중.. 호두랑 놀아주세요!",
                "닉네임1",
                listOf(
                    Pet(name = "호두", breed = "포메라니안")
                ),
                10,
                5
            ))
            add(Post(
                1,
                "2025년! 새해 복 많이 받으세요!!",
                "우리 호두도 올해로 5살이 되었어요. 새해 아침부터 터그놀이 하는 중.. 호두랑 놀아주세요!",
                "닉네임1",
                listOf(
                    Pet(name = "호두", breed = "포메라니안")
                ),
                10,
                5
            ))
        }

        // 인기 게시물 recycler view
        val homeHotPostRVAdapter = HomeHotPostRVAdapter(hotPostDatas)
        binding.homeHotPostRv.adapter = homeHotPostRVAdapter
        binding.homeHotPostRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        // 클릭 인터페이스
        homeHotPostRVAdapter.setMyItemClickListener(object : HomeHotPostRVAdapter.OnItemClickListener {
            override fun onItemClick(post: Post) {
                // post 연결
            }
        })

    }

    private fun setupEventRecyclerView() {
        eventDatas.clear()

        // 이벤트 recycler view
        val homePetEventRVAdapter = HomePetEventRVAdapter(eventDatas)
        binding.homePetEventRv.adapter = homePetEventRVAdapter
        binding.homePetEventRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        getAllEvents(homePetEventRVAdapter)

        // 클릭 인터페이스
        homePetEventRVAdapter.setMyItemClickListener(object : HomePetEventRVAdapter.OnItemClickListener {
            override fun onItemClick(event: Event) {
                // event 연결
                val uri = Uri.parse(event.eventUrl);
                val it = Intent(Intent.ACTION_VIEW, uri);
                startActivity(it)
            }
        })
    }

    private fun getAllEvents(adapter: HomePetEventRVAdapter) {
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
                        }.take(2)

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

    // d_day 설정 창
}