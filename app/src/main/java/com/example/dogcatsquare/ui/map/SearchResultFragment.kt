package com.example.dogcatsquare.ui.map

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.RetrofitClient
import com.example.dogcatsquare.data.map.MapPlace
import com.example.dogcatsquare.data.map.SearchPlacesRequest
import com.example.dogcatsquare.databinding.FragmentSearchResultBinding
import com.example.dogcatsquare.ui.map.location.MapDetailFragment
import com.example.dogcatsquare.ui.map.location.MapEtcFragment
import com.example.dogcatsquare.ui.map.location.MapPlaceRVAdapter
import com.example.dogcatsquare.ui.map.walking.WalkingStartViewFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchResultFragment : Fragment() {
    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!

    private val placeDatas = ArrayList<MapPlace>()
    private lateinit var mapPlaceRVAdapter: MapPlaceRVAdapter
    private var searchQuery: String? = null

    private var latitude: Double = 37.5665
    private var longitude: Double = 126.9780

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchQuery = arguments?.getString("searchQuery")
        latitude = arguments?.getDouble("latitude", 37.5665) ?: 37.5665
        longitude = arguments?.getDouble("longitude", 126.9780) ?: 126.9780
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()

        // 검색어가 있다면 검색 실행
        searchQuery?.let { query ->
            loadPlaces(query)
        }
    }

    private fun setupRecyclerView() {
        mapPlaceRVAdapter = MapPlaceRVAdapter(placeDatas, object : MapPlaceRVAdapter.OnItemClickListener {
            override fun onItemClick(place: MapPlace) {
                when (place.placeType) {
                    "동물병원" -> {
                        val fragment = MapDetailFragment().apply {
                            arguments = Bundle().apply {
                                putInt("placeId", place.id)
                                val (lat, lng) = getCurrentLocation()
                                putDouble("latitude", lat)
                                putDouble("longitude", lng)
                            }
                        }
                        navigateToFragment(fragment)
                    }
                    "산책로" -> {
                        navigateToFragment(WalkingStartViewFragment())
                    }
                    else -> {
                        val fragment = MapEtcFragment().apply {
                            arguments = Bundle().apply {
                                putInt("placeId", place.id)
                                val (lat, lng) = getCurrentLocation()
                                putDouble("latitude", lat)
                                putDouble("longitude", lng)
                            }
                        }
                        navigateToFragment(fragment)
                    }
                }
            }
        })

        binding.mapPlaceRV.apply {
            adapter = mapPlaceRVAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun loadPlaces(keyword: String) {
        val token = getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("SearchResultFragment", "원본 검색어: '$keyword'")

        val formattedKeyword = formatSearchKeyword(keyword)
        Log.d("SearchResultFragment", "전처리된 검색어: '$formattedKeyword'")

        lifecycleScope.launch {
            try {
                val searchRequest = SearchPlacesRequest(
                    latitude = latitude,
                    longitude = longitude
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.placesApiService.searchPlaces(
                        token = "Bearer $token",
//                        cityId = getCityId(),
//                        keyword = keyword,
                        request = searchRequest
                    )
                }

                Log.d("SearchResultFragment", "API 응답: isSuccess=${response.isSuccess}, " +
                        "결과 수=${response.result?.content?.size ?: 0}")

                if (response.isSuccess) {
                    val places = response.result?.content?.map { place ->
                        MapPlace(
                            id = place.id,
                            placeName = place.name,
                            placeType = convertCategory(place.category),
                            placeDistance = "${String.format("%.2f", place.distance)}km",
                            placeLocation = place.address,
                            placeCall = place.phoneNumber,
                            isOpen = if (place.open) "영업중" else "영업종료",
                            placeImgUrl = place.imgUrl,
                            reviewCount = place.reviewCount
                        )
                    } ?: emptyList()

                    updateRecyclerView(places)

                    // 검색 결과 수 표시
                    Toast.makeText(
                        requireContext(),
                        "총 ${places.size}개의 검색 결과를 찾았습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.message ?: "검색 결과를 불러오는데 실패했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun formatSearchKeyword(keyword: String): String {
        // 검색어 전처리
        return keyword.trim()  // 기본적인 공백 제거
    }

    private fun updateRecyclerView(newPlaces: List<MapPlace>) {
        placeDatas.clear()
        placeDatas.addAll(newPlaces)
        mapPlaceRVAdapter.notifyDataSetChanged()
    }

    private fun convertCategory(category: String): String {
        return when (category) {
            "HOSPITAL" -> "동물병원"
            "PARK" -> "산책로"
            "CAFE" -> "카페"
            "RESTAURANT" -> "식당"
            "HOTEL" -> "호텔"
            else -> category
        }
    }

    private fun getCurrentLocation(): Pair<Double, Double> {
        return Pair(latitude, longitude)
    }

    private fun getToken(): String? {
        return activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getString("token", null)
    }

    private fun getCityId(): Int {
        return activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getInt("city_id", 1) ?: 1
    }

    private fun handleError(e: Exception) {
        val errorMessage = when (e) {
            is retrofit2.HttpException -> {
                when (e.code()) {
                    401 -> "로그인이 필요합니다."
                    403 -> "권한이 없습니다."
                    404 -> "검색 결과를 찾을 수 없습니다."
                    else -> "서버 오류가 발생했습니다. (${e.code()})"
                }
            }
            is java.io.IOException -> "네트워크 연결을 확인해주세요."
            else -> "알 수 없는 오류가 발생했습니다: ${e.message}"
        }

        activity?.runOnUiThread {
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
        Log.e("SearchResultFragment", "검색 중 오류 발생", e)
    }

    private fun navigateToFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .hide(this)
            .add(R.id.main_frm, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("searchQuery", searchQuery)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let { bundle ->
            searchQuery = bundle.getString("searchQuery")
        }
    }
}