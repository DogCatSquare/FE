package com.example.dogcatsquare.ui.map

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.LoadingDialog
import com.example.dogcatsquare.R
import com.example.dogcatsquare.RetrofitClient
import com.example.dogcatsquare.data.map.MapPlace
import com.example.dogcatsquare.data.map.SearchPlacesRequest
import com.example.dogcatsquare.databinding.FragmentSearchResultBinding
import com.example.dogcatsquare.ui.map.location.MapDetailFragment
import com.example.dogcatsquare.ui.map.location.MapPlaceRVAdapter
import com.example.dogcatsquare.ui.map.walking.WalkingStartViewFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchResultFragment : Fragment() {
    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!

    // 어댑터가 데이터를 직접 관리하므로, 이 리스트는 더 이상 어댑터에 전달할 필요가 없습니다.
    private val placeDatas = ArrayList<MapPlace>()
    private lateinit var mapPlaceRVAdapter: MapPlaceRVAdapter
    private var searchQuery: String? = null

    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false
    private val ITEMS_PER_PAGE = 10

    private var latitude: Double = 37.5665
    private var longitude: Double = 126.9780

    private var shouldRefresh = false

    private lateinit var loadingDialog: LoadingDialog

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val loadTriggerPosition = (currentPage * ITEMS_PER_PAGE) + 5

            if (!isLoading && !isLastPage) {
                val lastVisibleItemPosition = firstVisibleItemPosition + visibleItemCount - 1
                if (firstVisibleItemPosition <= loadTriggerPosition &&
                    loadTriggerPosition <= lastVisibleItemPosition) {
                    loadMorePlaces()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchQuery = arguments?.getString("searchQuery")
        latitude = arguments?.getDouble("latitude", 37.5665) ?: 37.5665
        longitude = arguments?.getDouble("longitude", 126.9780) ?: 126.9780
        loadingDialog = LoadingDialog(requireContext())
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

        searchQuery?.let { query ->
            resetPagingState()
            loadPlaces(query, 0, true)
        }
    }

    private fun setupRecyclerView() {
        // 1. MapPlaceRVAdapter 생성자 수정: 리스트를 전달하지 않고, 클릭 리스너만 전달합니다.
        mapPlaceRVAdapter = MapPlaceRVAdapter(object : MapPlaceRVAdapter.OnItemClickListener {
            override fun onItemClick(place: MapPlace) {
                // 2. placeType 비교 값 수정: "HOSPITAL", "PARK" 대신 변환된 값인 "동물병원", "산책로"와 비교합니다.
                when (place.placeType) {
                    "동물병원" -> navigateToDetailFragment(place.id)
                    "산책로" -> navigateToFragment(WalkingStartViewFragment())
                    else -> navigateToDetailFragment(place.id)
                }
            }
        })

        binding.mapPlaceRV.apply {
            adapter = mapPlaceRVAdapter
            layoutManager = LinearLayoutManager(context)
            addOnScrollListener(scrollListener)
        }
    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun loadMorePlaces() {
        searchQuery?.let { query ->
            loadPlaces(query, currentPage + 1, false)
        }
    }

    private fun loadPlaces(keyword: String, page: Int, isNewSearch: Boolean) {
        if (isLoading) return
        val token = getToken() ?: return

        isLoading = true
        lifecycleScope.launch {
            try {
                if (isNewSearch) {
                    withContext(Dispatchers.Main) {
                        if (!loadingDialog.isDialogShowing) {
                            loadingDialog.show()
                        }
                    }
                }

                val searchRequest = SearchPlacesRequest(
                    latitude = latitude,
                    longitude = longitude
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.placesApiService.searchPlaces(
                        token = "Bearer $token",
                        keyword = keyword,
                        page = page,
                        request = searchRequest
                    )
                }

                if (response.isSuccess) {
                    val newPlaces = response.result?.content?.map { place ->
                        MapPlace(
                            id = place.id,
                            placeName = place.name,
                            placeType = convertCategory(place.category),
                            placeDistance = "${String.format("%.2f", place.distance)}km",
                            placeLocation = place.address,
                            placeCall = place.phoneNumber,
                            isOpen = if (place.open) "영업중" else "영업종료",
                            placeImgUrl = place.imgUrl,
                            reviewCount = place.reviewCount,
                            keywords = place.keywords ?: emptyList()
                        )
                    } ?: emptyList()

                    currentPage = response.result?.number ?: 0
                    isLastPage = response.result?.last ?: true

                    withContext(Dispatchers.Main) {
                        if (isNewSearch) {
                            updateRecyclerView(newPlaces)
                            // 검색 결과가 없을 때 noResultLayout 표시
                            binding.noResultLayout.visibility = if (newPlaces.isEmpty()) View.VISIBLE else View.GONE
                            binding.mapPlaceRV.visibility = if (newPlaces.isEmpty()) View.GONE else View.VISIBLE
                        } else {
                            appendToRecyclerView(newPlaces)
                        }
                    }
                }
            } catch (e: Exception) {
                // 에러 발생 시 처리
            } finally {
                isLoading = false
                // 새로운 검색일 경우에만 로딩 다이얼로그 닫기
                if (isNewSearch) {
                    withContext(Dispatchers.Main) {
                        if (loadingDialog.isDialogShowing) {
                            loadingDialog.dismiss()
                        }
                    }
                }
            }
        }
    }

    private fun updateRecyclerView(newPlaces: List<MapPlace>) {
        placeDatas.clear()
        placeDatas.addAll(newPlaces)
        // 3. 어댑터의 submitList 메서드를 사용하여 데이터 갱신
        mapPlaceRVAdapter.submitList(placeDatas)
    }

    private fun appendToRecyclerView(newPlaces: List<MapPlace>) {
        placeDatas.addAll(newPlaces)
        // 3. 어댑터의 submitList 메서드를 사용하여 데이터 갱신
        mapPlaceRVAdapter.submitList(placeDatas)
    }

    private fun navigateToDetailFragment(placeId: Int) {
        val fragment = MapDetailFragment().apply {
            arguments = Bundle().apply {
                putInt("placeId", placeId)
                putDouble("latitude", latitude)
                putDouble("longitude", longitude)
            }
        }
        navigateToFragment(fragment)
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

    private fun getToken(): String? {
        return activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getString("token", null)
    }

    private fun resetPagingState() {
        currentPage = 0
        isLastPage = false
        isLoading = false
        placeDatas.clear()
        // 3. 어댑터의 submitList 메서드를 사용하여 데이터 갱신
        mapPlaceRVAdapter.submitList(placeDatas)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 로딩 다이얼로그가 표시되어 있다면 닫기
        if (::loadingDialog.isInitialized && loadingDialog.isDialogShowing) {
            loadingDialog.dismiss()
        }
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        // 이전 화면에서 돌아왔을 때 데이터 새로고침
        if (shouldRefresh) {
            searchQuery?.let { query ->
                resetPagingState()
                loadPlaces(query, 0, true)
            }
            shouldRefresh = false
        }
    }

    override fun onPause() {
        super.onPause()
        // 다른 프래그먼트로 이동할 때 새로고침 플래그 설정
        shouldRefresh = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("searchQuery", searchQuery)
        outState.putInt("currentPage", currentPage)
        outState.putBoolean("isLastPage", isLastPage)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let { bundle ->
            searchQuery = bundle.getString("searchQuery")
            currentPage = bundle.getInt("currentPage", 0)
            isLastPage = bundle.getBoolean("isLastPage", false)
        }
    }
}