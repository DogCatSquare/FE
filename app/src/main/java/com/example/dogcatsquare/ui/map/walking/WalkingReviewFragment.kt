package com.example.dogcatsquare.ui.map.walking

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.example.dogcatsquare.R
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkReviewViewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PolylineOverlay
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.jar.Manifest

class WalkingReviewFragment : Fragment(), OnMapReadyCallback {

    private lateinit var naverMap: NaverMap
    private var routeCoords: ArrayList<LatLng> = arrayListOf()
    private var elapsedMinutes: Long = 0L
    private lateinit var addedImageView: ImageView

    // 후기 입력 UI
    private lateinit var reviewContentEditText: EditText
    private lateinit var submitReviewButton: Button

    // ViewModel for API 호출
    private lateinit var viewModel: WalkReviewViewModel

    private val PICK_IMAGE_REQUEST = 1001
    private var selectedImageUri: Uri? = null

    // 예시 산책로 ID (실제 값으로 초기화 필요)
    private var walkId: Long = 20L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_mapwalking_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ViewModel 초기화
        viewModel = ViewModelProvider(this)[WalkReviewViewModel::class.java]

        // 툴바 설정
        (activity as? AppCompatActivity)?.apply {
            val toolbar: Toolbar = view.findViewById(R.id.walking_review_toolbar)
            setSupportActionBar(toolbar)
            supportActionBar?.title = "산책 기록"
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        // Bundle에서 경과 시간(분)과 경로 좌표 목록을 받음
        elapsedMinutes = arguments?.getLong("elapsedTime", 0L) ?: 0L
        routeCoords = arguments?.getParcelableArrayList("routeCoords") ?: arrayListOf()

        // 경과 시간 텍스트 업데이트 (예: "30분")
        val minTv: TextView = view.findViewById(R.id.min_tv)
        minTv.text = "$elapsedMinutes 분"

        // ImageView 초기화
        addedImageView = view.findViewById(R.id.addedImageView)

        // 갤러리 오픈 버튼 설정
        val addImgButton: AppCompatImageButton = view.findViewById(R.id.addImg_bt)
        addImgButton.setOnClickListener {
            openGallery()
        }

        // 지도 프래그먼트 설정
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as? MapFragment
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().add(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync(this)

        // Completion 버튼 (다음 화면으로 이동)
        val completionButton: Button = view.findViewById(R.id.Completion_bt)
        completionButton.setOnClickListener {
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.main_frm, WalkingReviewTypeFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        reviewContentEditText = view.findViewById(R.id.introduction_tv)
        submitReviewButton = view.findViewById(R.id.Completion_bt)

        submitReviewButton.setOnClickListener {
            val content = reviewContentEditText.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(requireContext(), "후기 내용을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri == null) {
                Toast.makeText(requireContext(), "후기 이미지는 필수입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val token = getToken()
            Log.d("Token", "Token: $token")
            if (token == null) {
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 후기 저장 API 호출
            viewModel.saveWalkReview(walkId, content, selectedImageUri, requireContext())
        }

        // 후기 제출 성공 시 처리
        viewModel.reviewResponse.observe(viewLifecycleOwner) { response ->
            response?.let {
                Log.d("WalkReview", "후기 제출 성공: $it")
                Toast.makeText(requireContext(), "산책로 후기 작성 완료!", Toast.LENGTH_SHORT).show()

                val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.main_frm, WalkingReviewTypeFragment())
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            try {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedImageUri)
                selectedImageUri?.let {
                    addedImageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        naverMap.uiSettings.isLocationButtonEnabled = true
        naverMap.locationTrackingMode = LocationTrackingMode.None

        if (routeCoords.isNotEmpty()) {
            val polyline = PolylineOverlay().apply {
                coords = routeCoords
                width = 10
                color = Color.parseColor("#FFB200")
            }
            polyline.map = naverMap

            val startMarker = Marker().apply {
                position = routeCoords.first()
                icon = OverlayImage.fromResource(R.drawable.ic_start_marker)
                map = naverMap
            }
            val endMarker = Marker().apply {
                position = routeCoords.last()
                icon = OverlayImage.fromResource(R.drawable.ic_end_marker)
                map = naverMap
            }
            val cameraUpdate = CameraUpdate.scrollTo(routeCoords.first())
            naverMap.moveCamera(cameraUpdate)

            val totalDistanceKm = calculateTotalDistance(routeCoords)
            val kmTv: TextView = view?.findViewById(R.id.km_tv) ?: return
            kmTv.text = String.format("%.1f km", totalDistanceKm)
        }
    }

    private fun getToken(): String? {
        return activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getString("token", null)
    }

    private fun calculateTotalDistance(coords: List<LatLng>): Float {
        var totalDistance = 0f
        for (i in 0 until coords.size - 1) {
            val start = coords[i]
            val end = coords[i + 1]
            val results = FloatArray(1)
            Location.distanceBetween(
                start.latitude, start.longitude,
                end.latitude, end.longitude,
                results
            )
            totalDistance += results[0]
        }
        return totalDistance / 1000f
    }
}
