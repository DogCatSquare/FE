package com.example.dogcatsquare.ui.map.walking

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.LoadingDialog
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.model.map.PlaceDetailRequest
import com.example.dogcatsquare.data.model.walk.ReportRequest
import com.example.dogcatsquare.data.model.walk.Walk
import com.example.dogcatsquare.data.network.RetrofitClient
import com.example.dogcatsquare.databinding.FragmentMapwalkingBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalkingMapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapwalkingBinding? = null
    private val binding get() = _binding!!

    private lateinit var walkRVAdapter: WalkRVAdapter
    private var googlePlaceId: String = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private var googleMap: GoogleMap? = null
    private var currentMarker: Marker? = null
    private var isWished = false
    private lateinit var loadingDialog: LoadingDialog

    data class ErrorResponse(
        val isSuccess: Boolean? = null,
        val code: String? = null,
        val message: String? = null,
        val result: Any? = null
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())
        arguments?.let {
            googlePlaceId = it.getString("googlePlaceId", "") ?: ""
            latitude = it.getDouble("latitude", 0.0)
            longitude = it.getDouble("longitude", 0.0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapwalkingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGoogleMap()
        setupRecyclerView()
        setupButtons()

        if (googlePlaceId.isNotEmpty()) {
            loadPlaceDetails(googlePlaceId)
        }
    }

    private fun setupGoogleMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapView3) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun setupRecyclerView() {
        walkRVAdapter = WalkRVAdapter(
            onItemClick = { walkId ->
                val fragment = WalkingStartViewFragment.newInstance(walkId, googlePlaceId)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onMenuClick = { walk, anchorView ->
                showWalkMenu(walk, anchorView)
            },
            walkList = arrayListOf()
        )

        binding.reviewRv.apply {
            adapter = walkRVAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun showWalkMenu(walk: Walk, anchorView: View) {
        val popupMenu = PopupMenu(requireContext(), anchorView)
        popupMenu.menuInflater.inflate(R.menu.walk_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    AlertDialog.Builder(requireContext())
                        .setMessage("이 산책로를 삭제할까요?")
                        .setPositiveButton("삭제") { _, _ ->
                            deleteWalk(walk.walkId)
                        }
                        .setNegativeButton("취소", null)
                        .show()
                    true
                }

                R.id.action_report -> {
                    showReportTypeDialog(walk.walkId)
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showReportTypeDialog(walkId: Int) {
        val reportLabels = arrayOf(
            "홍보성",
            "욕설 비방 혐오",
            "음란 선정성",
            "도배",
            "개인정보 노출",
            "기타"
        )

        val reportTypes = arrayOf(
            "ADVERTISEMENT",
            "ABUSE_HATE_SPEECH",
            "ADULT_CONTENT",
            "SPAM",
            "PERSONAL_INFO",
            "OTHER"
        )

        AlertDialog.Builder(requireContext())
            .setTitle("신고 사유를 선택하세요")
            .setItems(reportLabels) { _, which ->
                val selectedType = reportTypes[which]
                if (selectedType == "OTHER") {
                    reportWalk(walkId, selectedType, "기타")
                } else {
                    reportWalk(walkId, selectedType, null)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun setupButtons() {
        binding.apply {
            backButton.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }

            reviewAllBt.setOnClickListener {
                val fragment = WalkingListFragment().apply {
                    arguments = Bundle().apply {
                        putString("walkName", binding.placeName.text.toString())
                    }
                }
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            addButton.setOnClickListener {
                val currentPlaceName = binding.placeName.text.toString()

                val fragment = WalkingMapViewFragment().apply {
                    arguments = Bundle().apply {
                        putString("placeName", currentPlaceName)
                    }
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            wishButton.setOnClickListener {
                toggleWish(googlePlaceId)
            }
        }
    }

    private fun loadPlaceDetails(googlePlaceId: String) {
        Log.d("WalkingMapFragment", "🚀 loadPlaceDetails 시작 - googlePlaceId: $googlePlaceId")
        if (!loadingDialog.isDialogShowing) loadingDialog.show()

        lifecycleScope.launch {
            try {
                val token = getToken()
                if (token == null) {
                    Log.e("WalkingMapFragment", "❌ 토큰 없음: 로그인이 필요함")
                    Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val request = PlaceDetailRequest(
                    latitude = latitude,
                    longitude = longitude
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.placesApiService.getPlaceById(
                        token = "Bearer $token",
                        googlePlaceId = googlePlaceId,
                        request = request
                    )
                }

                if (response.isSuccess) {
                    response.result?.let { placeDetail ->
                        binding.apply {
                            placeName.text = placeDetail.name
                            placeLocation.text = placeDetail.address.split(" ").getOrNull(2) ?: ""
                            placeDistance.text = "${String.format("%.2f", placeDetail.distance)}km"
                            addressTv.text = placeDetail.address

                            isWished = placeDetail.wished
                            wishButton.setImageResource(
                                if (isWished) R.drawable.ic_wish_check else R.drawable.ic_wish
                            )
                        }

                        googleMap?.let {
                            updateMapLocation(placeDetail.latitude, placeDetail.longitude)
                        }

                        try {
                            val walkResponse = withContext(Dispatchers.IO) {
                                RetrofitClient.walkApiService.searchWalks(title = placeDetail.name)
                            }

                            binding.apply {
                                placeType.text = "리뷰(${walkResponse.walks.size})"
                                rightText.text = walkResponse.walks.size.toString()

                                if (walkResponse.walks.isEmpty()) {
                                    reviewRv.visibility = View.GONE
                                    defaultWalkText.visibility = View.VISIBLE
                                    rightText.visibility = View.GONE
                                    reviewAllBt.visibility = View.GONE
                                } else {
                                    reviewRv.visibility = View.VISIBLE
                                    defaultWalkText.visibility = View.GONE
                                    rightText.visibility = View.VISIBLE
                                    reviewAllBt.visibility = View.VISIBLE
                                    walkRVAdapter.updateData(ArrayList(walkResponse.walks))
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("WalkingMapFragment", "⚠️ 산책로 검색 실패: ${e.message}")
                            binding.apply {
                                placeType.text = "리뷰(0)"
                                reviewRv.visibility = View.GONE
                                defaultWalkText.visibility = View.VISIBLE
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.message ?: "상세 정보를 불러오는데 실패했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("WalkingMapFragment", "💥 예외 발생: ${e.message}", e)
                handleError(e)
            } finally {
                if (loadingDialog.isDialogShowing) loadingDialog.dismiss()
            }
        }
    }

    private fun toggleWish(googlePlaceId: String) {
        lifecycleScope.launch {
            try {
                val token = getToken()
                if (token == null) {
                    Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.placesApiService.toggleWish(
                        token = "Bearer $token",
                        googlePlaceId = googlePlaceId
                    )
                }

                if (response.isSuccess) {
                    isWished = response.result ?: !isWished
                    binding.wishButton.setImageResource(
                        if (isWished) R.drawable.ic_wish_check else R.drawable.ic_wish
                    )

                    Toast.makeText(
                        requireContext(),
                        if (isWished) "위시리스트에 추가되었습니다." else "위시리스트에서 제외되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.message ?: "오류가 발생했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun deleteWalk(walkId: Int) {
        lifecycleScope.launch {
            try {
                val token = getToken()
                if (token == null) {
                    Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.walkApiService.deleteWalk(
                        token = "Bearer $token",
                        walkId = walkId
                    )
                }

                if (response.isSuccess) {
                    Toast.makeText(requireContext(), "산책로가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    loadPlaceDetails(googlePlaceId)
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.message ?: "산책로 삭제에 실패했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun reportWalk(walkId: Int, reportType: String, otherReason: String?) {
        lifecycleScope.launch {
            try {
                val token = getToken()
                if (token == null) {
                    Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.walkApiService.reportWalk(
                        token = "Bearer $token",
                        walkId = walkId,
                        body = ReportRequest(
                            reportType = reportType,
                            otherReason = otherReason
                        )
                    )
                }

                Toast.makeText(
                    requireContext(),
                    response.message ?: "산책로가 신고되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.apply {
            isZoomControlsEnabled = false
            isScrollGesturesEnabled = false
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
            isZoomGesturesEnabled = false
            isMapToolbarEnabled = false
        }
        updateMapLocation(latitude, longitude)
    }

    private fun updateMapLocation(lat: Double, lng: Double) {
        val location = LatLng(lat, lng)

        currentMarker?.remove()

        val icon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_marker)
        currentMarker = googleMap?.addMarker(
            MarkerOptions()
                .position(location)
                .icon(icon)
        )

        googleMap?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(location, 15.0f)
        )
    }

    private fun bitmapDescriptorFromVector(
        context: Context,
        @DrawableRes vectorResId: Int
    ): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(
                intrinsicWidth,
                intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            draw(canvas)
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    private fun getToken(): String? {
        return activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getString("token", null)
    }

    private fun parseErrorMessage(e: retrofit2.HttpException): String? {
        return try {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("WalkingMapFragment", "errorBody = $errorBody")

            if (errorBody.isNullOrBlank()) return null

            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            val resultMessage = errorResponse.result as? String

            when {
                !resultMessage.isNullOrBlank() -> resultMessage
                !errorResponse.message.isNullOrBlank() -> errorResponse.message
                else -> null
            }
        } catch (ex: Exception) {
            Log.e("WalkingMapFragment", "parseErrorMessage 실패", ex)
            null
        }
    }

    private fun handleError(e: Exception) {
        val errorMessage = when (e) {
            is retrofit2.HttpException -> {
                parseErrorMessage(e) ?: when (e.code()) {
                    401 -> "잘못된 토큰입니다."
                    403 -> "권한이 없습니다."
                    404 -> "데이터를 찾을 수 없습니다."
                    else -> "서버 오류가 발생했습니다. (${e.code()})"
                }
            }
            is java.io.IOException -> "네트워크 연결을 확인해주세요."
            else -> "알 수 없는 오류가 발생했습니다: ${e.message}"
        }

        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        Log.e("WalkingMapFragment", "Error: ", e)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        googleMap = null
    }

    override fun onStop() {
        super.onStop()
        if (::loadingDialog.isInitialized && loadingDialog.isDialogShowing) {
            loadingDialog.dismiss()
        }
    }

    companion object {
        fun newInstance(
            googlePlaceId: String,
            latitude: Double,
            longitude: Double
        ): WalkingMapFragment {
            return WalkingMapFragment().apply {
                arguments = Bundle().apply {
                    putString("googlePlaceId", googlePlaceId)
                    putDouble("latitude", latitude)
                    putDouble("longitude", longitude)
                }
            }
        }
    }
}