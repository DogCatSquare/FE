package com.example.dogcatsquare.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.MapButton
import com.example.dogcatsquare.MapButtonRVAdapter
import com.example.dogcatsquare.MapPlace
import com.example.dogcatsquare.MapPlaceRVAdapter
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentMapBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private val buttonDatas by lazy { ArrayList<MapButton>() }
    private val placeDatas by lazy { ArrayList<MapPlace>() }

    // 위치 관련 변수
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupBottomSheet()
        setupNaverMap()
    }

    private fun setupNaverMap() {
        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(R.id.mapView) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.mapView, it).commit()
            }

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map

        // 위치 소스 지정
        naverMap.locationSource = locationSource

        // 현재 위치 버튼 활성화
        naverMap.uiSettings.isLocationButtonEnabled = true

        // 위치 추적 모드 설정
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        // 위치 권한 확인
        if (hasLocationPermission()) {
            enableCurrentLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun enableCurrentLocation() {
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults
            )) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            } else {
                naverMap.locationTrackingMode = LocationTrackingMode.Follow
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setupRecyclerView() {
        buttonDatas.clear()
        placeDatas.clear()

        buttonDatas.apply {
            add(MapButton("전체"))
            add(MapButton("병원", R.drawable.ic_hospital))
            add(MapButton("산책로", R.drawable.ic_walk))
            add(MapButton("음식/카페", R.drawable.ic_restaurant))
            add(MapButton("호텔", R.drawable.ic_hotel))
        }

        val mapButtonRVAdapter = MapButtonRVAdapter(buttonDatas, object : MapButtonRVAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, buttonName: String) {
                // 각 버튼별 처리
                when (buttonName) {
                    "전체" -> {
                        placeDatas.clear()
                        placeDatas.addAll(getAllPlaces())
                        binding.mapPlaceRV.adapter?.notifyDataSetChanged()
                    }
                    "병원" -> {
                        placeDatas.clear()
                        placeDatas.addAll(getAllPlaces().filter { it.placeType == "동물병원" })
                        binding.mapPlaceRV.adapter?.notifyDataSetChanged()
                    }
                    "산책로" -> {
                        placeDatas.clear()
                        placeDatas.addAll(getAllPlaces().filter { it.placeType == "산책로" })
                        binding.mapPlaceRV.adapter?.notifyDataSetChanged()
                    }
                    "음식/카페" -> {
                        placeDatas.clear()
                        placeDatas.addAll(getAllPlaces().filter { it.placeType == "음식/카페" })
                        binding.mapPlaceRV.adapter?.notifyDataSetChanged()
                    }
                    "호텔" -> {
                        placeDatas.clear()
                        placeDatas.addAll(getAllPlaces().filter { it.placeType == "호텔" })
                        binding.mapPlaceRV.adapter?.notifyDataSetChanged()
                    }
                }
            }
        })

        binding.mapButtonRV.apply {
            adapter = mapButtonRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        placeDatas.addAll(getAllPlaces())

        val mapPlaceRVAdapter = MapPlaceRVAdapter(placeDatas, object : MapPlaceRVAdapter.OnItemClickListener {
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
                            putString("placeChar1", place.placeChar1)
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
                            putString("placeChar1", place.placeChar1)
                            place.placeImg?.let { putInt("placeImg", it) }
                        }
                    }
                }

                // Fragment 전환
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        })

        binding.mapPlaceRV.apply {
            adapter = mapPlaceRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun getAllPlaces(): List<MapPlace> {
        return listOf(
            MapPlace("가나다 동물병원", "동물병원", "0.55km", "서울시 성북구 월곡동 77", "02-1234-5678", "중성화 수술", R.drawable.ic_place_img_default),
            MapPlace("서대문 안산자락길", "산책로", "0.55km", "서울시 서대문구 봉원사길 75-66", "02-1234-5678", "쓰레기통", R.drawable.ic_place_img_default),
            MapPlace("다나가 동물병원", "동물병원", "0.55km", "서울시 성북구 월곡동 77", "02-1234-5678", "중성화 수술", R.drawable.ic_place_img_default),
            MapPlace("가나다 동물병원", "동물병원", "0.55km", "서울시 성북구 월곡동 77", "02-1234-5678", "중성화 수술", R.drawable.ic_place_img_default),
            MapPlace("서대문 안산자락길", "산책로", "0.55km", "서울시 서대문구 봉원사길 75-66", "02-1234-5678", "쓰레기통", R.drawable.ic_place_img_default),
            MapPlace("가나다 동물병원", "동물병원", "0.55km", "서울시 성북구 월곡동 77", "02-1234-5678", "중성화 수술", R.drawable.ic_place_img_default),
            MapPlace("가나다 동물병원", "동물병원", "0.55km", "서울시 성북구 월곡동 77", "02-1234-5678", "중성화 수술", R.drawable.ic_place_img_default)
        )
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)

        binding.root.post {
            val mapButtonBottom = binding.mapButtonRV.bottom + (binding.mapButtonRV.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin

            bottomSheetBehavior.maxHeight = binding.root.height - mapButtonBottom
        }

        // 기본 설정
        bottomSheetBehavior.apply {
            isDraggable = true
            isHideable = false
            state = BottomSheetBehavior.STATE_COLLAPSED
        }

        // 콜백 설정
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        // BottomSheet이 최대로 확장되었을 때 MapFullFragment로 전환
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.main_frm, MapFullFragment())
                            .addToBackStack(null)
                            .commitAllowingStateLoss()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // 슬라이드 처리
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}