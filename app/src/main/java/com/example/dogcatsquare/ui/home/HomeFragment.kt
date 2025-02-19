package com.example.dogcatsquare.ui.home

import WeatherViewModel
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
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.api.BoardApiService
import com.example.dogcatsquare.data.api.DDayRetrofitItf
import com.example.dogcatsquare.data.api.EventRetrofitItf
import com.example.dogcatsquare.data.api.PlacesApiService
import com.example.dogcatsquare.data.map.GetHotPlaceRequest
import com.example.dogcatsquare.data.map.GetHotPlaceResponse
import com.example.dogcatsquare.data.map.Place
import com.example.dogcatsquare.data.model.home.DDay
import com.example.dogcatsquare.data.model.home.Event
import com.example.dogcatsquare.data.model.home.GetAllDDayResponse
import com.example.dogcatsquare.data.model.home.GetAllEventsResponse
import com.example.dogcatsquare.data.model.home.WeatherResult
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.data.model.post.PopularPostResponse
import com.example.dogcatsquare.data.model.post.Post
import com.example.dogcatsquare.databinding.FragmentHomeBinding
import com.example.dogcatsquare.ui.community.PostDetailActivity
import com.example.dogcatsquare.ui.map.location.MapDetailFragment
import com.example.dogcatsquare.ui.mypage.HorizontalSpacingItemDecoration
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Timer
import java.util.TimerTask

class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding

    private val viewModel: WeatherViewModel by activityViewModels()

    private var adTimer: Timer? = null
    private var weatherTimer: Timer? = null
    private val handler = Handler(Looper.getMainLooper())

    private var dDayDatas = ArrayList<DDay>()
    private var placeDatas = ArrayList<Place>()
    private var hotPostDatas = ArrayList<Post>()
    private var eventDatas = ArrayList<Event>()

    private fun getToken(): String? {
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

    private fun getCityId(): Long? {
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getLong("cityId", 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // 상단바 색깔
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.light_blue)

        fetchWeatherData()
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
        adTimer?.cancel() // 기존 타이머 중지
        adTimer = Timer() // 새로운 타이머 생성

        adTimer?.scheduleAtFixedRate(object : TimerTask() {
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
        }, 4500, 4500)
    }

    private fun weatherAutoSlide(adapter: HomeWeatherVPAdapter) {
        weatherTimer?.cancel() // 기존 타이머 중지
        weatherTimer = Timer() // 새로운 타이머 생성

        weatherTimer?.scheduleAtFixedRate(object : TimerTask() {
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
    private fun fetchWeatherData() {
        viewModel.weatherData.observe(viewLifecycleOwner) { weatherResponse ->
            if (weatherResponse != null) {
                Log.d("HomeFragment", "WeatherData 수신: ${weatherResponse.result.mainMessage}")
                setupWeatherViewPager(weatherResponse.result)
            } else {
                Log.e("HomeFragment", "WeatherData 수신 실패")
            }
        }
        viewModel.getWeatherData() // ViewModel에서 캐싱 확인 후 호출
    }

    private fun setupWeatherViewPager(weatherResult: WeatherResult) {
        // ad view pager
        val homeWeatherAdapter = HomeWeatherVPAdapter(this)

        if(weatherResult.ddayTitle != null) {
            homeWeatherAdapter.addFragment(HomeWeatherFragment.newInstance(weatherResult, 0))
            homeWeatherAdapter.addFragment(HomeWeatherFragment.newInstance(weatherResult, 1))

            binding.homeWeatherVp.adapter = homeWeatherAdapter
            binding.homeWeatherVp.orientation = ViewPager2.ORIENTATION_HORIZONTAL

            weatherAutoSlide(homeWeatherAdapter)
        } else {
            homeWeatherAdapter.addFragment(HomeWeatherFragment.newInstance(weatherResult, 0))

            binding.homeWeatherVp.adapter = homeWeatherAdapter
            binding.homeWeatherVp.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }
    }

    // d_day rv
    private fun setupDDayRecyclerView() {
        dDayDatas.clear()

        // d-day recycler view
        val dDayRVAdapter = HomeDDayRVAdapter(dDayDatas, requireContext())
        binding.homeDdayRv.adapter = dDayRVAdapter
        binding.homeDdayRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        binding.homeDdayRv.addItemDecoration(HorizontalSpacingItemDecoration(15))
        binding.homeDdayRv.setHasFixedSize(true)

        getAllDDay(dDayRVAdapter)

        // 클릭 인터페이스
        dDayRVAdapter.setMyItemClickListener(object: HomeDDayRVAdapter.OnItemClickListener {
            override fun onItemClick(d_day: DDay) {
                // id, title, day, term, isAlarm
                if (d_day.title.equals("사료 구매") || d_day.title.equals("패드/모래 구매") || d_day.title.equals("병원 방문일")) {
                    val fragment = SetDDayDefaultFragment().apply {
                        arguments = Bundle().apply {
                            putInt("ddayId", d_day.id)
                            putString("ddayTitle", d_day.title)
                            putString("ddayDay", d_day.day)
                            putInt("ddayTerm", d_day.term ?: 0)
                            putBoolean("isAlarm", d_day.isAlarm)
                        }
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, fragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
                } else {
                    val fragment = SetDDayPersonalFragment().apply {
                        arguments = Bundle().apply {
                            putInt("ddayId", d_day.id)
                            putString("ddayTitle", d_day.title)
                            putString("ddayDay", d_day.day)
                            putInt("ddayTerm", d_day.term ?: 0)
                            putBoolean("isAlarm", d_day.isAlarm)
                        }
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, fragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
                }
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
                                term  = dday.term ?: 0,
                                daysLeft = dday.daysLeft,
                                isAlarm = dday.isAlarm,
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

        // hot place recycler view
        val homeHotPlaceRVAdapter = HomeHotPlaceRVAdapter(placeDatas)
        binding.homeHotPlaceRv.adapter = homeHotPlaceRVAdapter
        binding.homeHotPlaceRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        getHotPlace(homeHotPlaceRVAdapter)

        // 클릭 인터페이스
        homeHotPlaceRVAdapter.setMyItemClickListener(object : HomeHotPlaceRVAdapter.OnItemClickListener {
            override fun onItemClick(place: Place) {
                val savedLocation = getSavedLocation()

                // placeType에 따라 다른 Fragment로 전환
                if (place.category == "HOSPITAL") {
                    val fragment = MapDetailFragment.newInstance(place.id, savedLocation?.first ?: 0.0, savedLocation?.second ?: 0.0)
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, fragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
                } else {
                    val fragment = MapDetailFragment.newInstance(place.id, savedLocation?.first ?: 0.0, savedLocation?.second ?: 0.0)
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, fragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
                }
            }
        })
    }

    private fun getHotPlace(adapter: HomeHotPlaceRVAdapter) {
        val token = getToken()
        val cityId = getCityId()
        val savedLocation = getSavedLocation()

        Log.d("loc", "$cityId, $savedLocation")

        val getPopularPlaceService = RetrofitObj.getRetrofit().create(PlacesApiService::class.java)
        if (savedLocation != null) {
            getPopularPlaceService.getHotPlace("Bearer $token", cityId, GetHotPlaceRequest(savedLocation.first, savedLocation.second)).enqueue(object : Callback<GetHotPlaceResponse> {
                override fun onResponse(call: Call<GetHotPlaceResponse>, response: Response<GetHotPlaceResponse>) {
                    Log.d("GetHotPlace/SUCCESS", response.toString())
                    val resp: GetHotPlaceResponse = response.body()!!

                    if (resp != null) {
                        if (resp.isSuccess) {
                            Log.d("GetHotPlace", "핫플 전체 조회 성공")

                            val places = resp.result.map { place ->
                                Place (
                                    id = place.id,
                                    name = place.name,
                                    address = place.address,
                                    category = place.category,
                                    phoneNumber = place.phoneNumber,
                                    longitude = place.longitude,
                                    latitude = place.latitude,
                                    distance = place.distance,
                                    open = place.open,
                                    imgUrl = place.imgUrl,
                                    reviewCount = place.reviewCount
                                )
                            }.take(5)

                            placeDatas.addAll(places)
                            Log.d("HotPlaceList", placeDatas.toString())
                            adapter.notifyDataSetChanged()
                        }

                    } else {
                        Log.e("GetHotPlace/ERROR", "응답 코드: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<GetHotPlaceResponse>, t: Throwable) {
                    Log.d("RETROFIT/FAILURE", t.message.toString())
                }

            })
        }
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

        // 인기 게시물 recycler view
        val homeHotPostRVAdapter = HomeHotPostRVAdapter(hotPostDatas)
        binding.homeHotPostRv.adapter = homeHotPostRVAdapter
        binding.homeHotPostRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        // 클릭 인터페이스
        homeHotPostRVAdapter.setMyItemClickListener(object : HomeHotPostRVAdapter.OnItemClickListener {
            override fun onItemClick(post: Post) {
                val intent = Intent(requireContext(), PostDetailActivity::class.java).apply {
                    putExtra("postId", post.id)
                }
                startActivity(intent)
            }
        })

        getPopularPost(homeHotPostRVAdapter)
    }

    private fun getPopularPost(adapter: HomeHotPostRVAdapter) {
        val token = getToken()

        val getPopularPostService = RetrofitObj.getRetrofit().create(BoardApiService::class.java)
        getPopularPostService.getPopularPost("Bearer $token").enqueue(object : Callback<PopularPostResponse> {
            override fun onResponse(call: Call<PopularPostResponse>, response: Response<PopularPostResponse>) {
                Log.d("PopularPost/SUCCESS", response.toString())
                val resp: PopularPostResponse = response.body()!!

                if (resp != null) {
                    if (resp.isSuccess) {
                        Log.d("PopularPost", "인기게시물 전체 조회 성공")

                        val posts = resp.result.map { post ->
                            Post (
                                id = post.id,
                                board = post.board,
                                title = post.title,
                                username = post.username,
                                content = post.content,
                                like_count = post.like_count,
                                comment_count = post.comment_count,
                                video_URL = post.video_URL,
                                thumbnail_URL = post.thumbnail_URL,
                                images = post.images,
                                createdAt = post.createdAt,
                                profileImage_URL = post.profileImage_URL
                            )
                        }.take(2)

                        hotPostDatas.addAll(posts)
                        Log.d("HotPostList", hotPostDatas.toString())
                        adapter.notifyDataSetChanged()
                    }

                } else {
                    Log.e("GetEvent/ERROR", "응답 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<PopularPostResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
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

    private fun getSavedLocation(): Pair<Double, Double>? {
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val latitude = sharedPref.getFloat("current_latitude", 0f)
        val longitude = sharedPref.getFloat("current_longitude", 0f)

        return if (latitude != -1f && longitude != -1f) {
            Pair(latitude.toDouble(), longitude.toDouble())
        } else {
            Pair(37.4683517, 127.0389883)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adTimer?.cancel()
        weatherTimer?.cancel()
    }
}