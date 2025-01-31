package com.example.dogcatsquare.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.dogcatsquare.data.map.MapPlace
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.home.DDay
import com.example.dogcatsquare.data.home.Event
import com.example.dogcatsquare.data.post.Pet
import com.example.dogcatsquare.data.post.Post
import com.example.dogcatsquare.databinding.FragmentHomeBinding
import com.example.dogcatsquare.ui.map.location.MapDetailFragment
import com.example.dogcatsquare.ui.map.location.MapEtcFragment
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

        // 디데이 임시 더미 데이터
        dDayDatas.apply {
            add(DDay("병원 방문까지", "D-35", R.drawable.ic_hospital))
            add(DDay("사료 주문까지", "D-35", R.drawable.ic_food))
            add(DDay("패드 주문까지", "D-35", R.drawable.ic_pad))
            add(DDay("", "", R.drawable.ic_set_d_day, isAddButton = true)) // 추가 버튼
        }

        // d-day recycler view
        val dDayRVAdapter = HomeDDayRVAdapter(dDayDatas)
        binding.homeDdayRv.adapter = dDayRVAdapter
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        layoutManager.isAutoMeasureEnabled = true
        binding.homeDdayRv.layoutManager = layoutManager

        requireActivity().supportFragmentManager.setFragmentResultListener("addDDayResult", this) { _, _ ->
            // 새 아이템 추가
//            dDayDatas.add(DDay("디데이까지", "D-35", R.drawable.ic_d_day))
//            dDayRVAdapter.notifyItemInserted(dDayDatas.size - 1) // 새 아이템만 추가
//            dDayRVAdapter.notifyDataSetChanged() // RecyclerView 업데이트

            // 추가 버튼의 위치를 찾음
            val addButtonPosition = dDayDatas.indexOfFirst { it.isAddButton }

            if (addButtonPosition != -1) {
                // 새 아이템을 추가 버튼 앞에 삽입
                dDayDatas.add(addButtonPosition, DDay("디데이까지", "D-35", R.drawable.ic_d_day))
                dDayRVAdapter.notifyItemInserted(addButtonPosition) // 새 아이템 추가
            }
        }

        // 클릭 인터페이스
        dDayRVAdapter.setMyItemClickListener(object: HomeDDayRVAdapter.OnItemClickListener {
            override fun onItemClick(d_day: DDay) {
                if (d_day.isAddButton) {
                    // "디데이 추가하기" 버튼 클릭 시
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, AddDDayFragment())
                        .addToBackStack("HomeFragment")
                        .commitAllowingStateLoss()
                } else {
                    // 일반 디데이 아이템 클릭 시
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, SetDDayFragment())
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
                }
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
                "가나다 동물병원",
                "동물병원",
                "0.55km",
                "서울시 성북구 월곡동 77",
                "02-1234-5678",
                "중성화 수술",    // char1Text
                "예방접종",       // char2Text
                "24시",          // char3Text
                R.drawable.ic_place_img_default,
                null))
            add(MapPlace(
                "서대문 안산자락길",
                "산책로",
                "0.55km",
                "서울시 서대문구 봉원사길 75-66 111111111111111111",
                "02-1234-5678",
                "난이도 하",
                "쓰레기통",
                null,
                R.drawable.ic_place_img_default,
                "리뷰(18)"))
            add(MapPlace("고양이호텔",
                "호텔",
                "0.55km",
                "서울시 성북구 월곡동 77",
                "02-1234-5678",
                "고양이탁묘",
                "고양이 보호소",
                null,
                R.drawable.ic_place_img_default,
                "리뷰(18)"))
            add(MapPlace(
                "가나다 동물병원",
                "동물병원",
                "0.55km",
                "서울시 성북구 월곡동 77",
                "02-1234-5678",
                "중성화 수술",    // char1Text
                "예방접종",       // char2Text
                "24시",          // char3Text
                R.drawable.ic_place_img_default,
                null))
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
                            putString("char1Text", place.char1Text)
                            place.placeImg?.let { putInt("placeImg", it) }
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
                            putString("char1Text", place.char1Text)
                            place.placeImg?.let { putInt("placeImg", it) }
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

        // 이벤트 임시 더미 데이터
        eventDatas.apply {
            add(Event("2025 케이펫페어 수원 시즌1", "2025.02.21 ~ 2025.02.23", R.drawable.img_event1))
            add(Event("2025 케이펫페어 수원 시즌1", "2025.02.21 ~ 2025.02.23", R.drawable.img_event2))
        }

        // 이벤트 recycler view
        val homePetEventRVAdapter = HomePetEventRVAdapter(eventDatas)
        binding.homePetEventRv.adapter = homePetEventRVAdapter
        binding.homePetEventRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        // 클릭 인터페이스
        homePetEventRVAdapter.setMyItemClickListener(object : HomePetEventRVAdapter.OnItemClickListener {
            override fun onItemClick(event: Event) {
                // event 연결
            }
        })
    }

    // d_day 설정 창
}