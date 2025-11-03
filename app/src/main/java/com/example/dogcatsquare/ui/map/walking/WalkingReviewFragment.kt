package com.example.dogcatsquare.ui.map.walking

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.example.dogcatsquare.R
import com.example.dogcatsquare.ui.map.walking.data.ViewModel.WalkReviewViewModel
// [수정됨] Naver Map import 제거
// import com.naver.maps.geometry.LatLng
// import com.naver.maps.map.*
// import com.naver.maps.map.overlay.Marker
// import com.naver.maps.map.overlay.OverlayImage
// import com.naver.maps.map.overlay.PolylineOverlay

// [수정됨] Google Map import 추가
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
// ---
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class WalkingReviewFragment : Fragment(), OnMapReadyCallback {

    // [수정됨] NaverMap -> GoogleMap
    private var googleMap: GoogleMap? = null
    // [수정됨] Naver LatLng -> Google LatLng
    private var routeCoords: ArrayList<LatLng> = arrayListOf()
    private var elapsedMinutes: Long = 0L
    private lateinit var addedImageView: ImageView

    // 후기 입력 UI
    private lateinit var reviewContentEditText: EditText
    private lateinit var submitReviewButton: Button

    // ViewModel for API 호출
    private lateinit var viewModel: WalkReviewViewModel

    private val PICK_IMAGE_REQUEST = 1001
    private var selectedBitmap: Bitmap? = null

    // 예시 산책로 ID (실제 값으로 초기화 필요)
    private var walkId: Long = 20L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // [수정 없음] fragment_mapwalking_review.xml을 로드
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
        // [수정됨] Google LatLng 타입으로 받도록 ArrayList 타입 변경
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

        // [수정됨] 지도 프래그먼트 설정 (Naver MapFragment -> Google SupportMapFragment)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
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

            if (selectedBitmap == null) {
                Toast.makeText(requireContext(), "후기 이미지는 필수입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val token = getToken()
            if (token.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val imagePart = bitmapToMultipart(selectedBitmap!!) // 이미지 변환

            // [수정됨] 예제 좌표를 Google LatLng로 변경 (ViewModel도 Google LatLng를 받도록 수정 필요)
            val routeCoords = listOf(LatLng(37.5665, 126.9780)) // 예제 좌표
            val elapsedMinutes = 30L
            val distance = 5.2f

            // [수정됨] createWalk 호출 (token 추가)
            // 참고: viewModel.createWalk 메서드도 Google LatLng 타입을 받도록 수정해야 합니다.
            viewModel.createWalk(token, routeCoords, elapsedMinutes, distance, content, imagePart)

            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()

            transaction.replace(R.id.main_frm, WalkingReviewTypeFragment())
            transaction.addToBackStack(null)
            transaction.commit()
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
            val selectedImageUri = data.data
            try {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, selectedImageUri)
                selectedBitmap = bitmap
                addedImageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // [수정됨] onMapReady(NaverMap) -> onMapReady(GoogleMap)
    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map

        googleMap?.uiSettings?.isMyLocationButtonEnabled = true

        if (routeCoords.isNotEmpty()) {
            // Polyline 그리기
            val polylineOptions = PolylineOptions()
                .addAll(routeCoords)
                .width(10f)
                .color(Color.parseColor("#FFB200"))
            googleMap?.addPolyline(polylineOptions)

            // 마커 아이콘 생성
            val startIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_start_marker)
            val endIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_end_marker)

            // 시작 마커
            googleMap?.addMarker(
                MarkerOptions()
                    .position(routeCoords.first())
                    .icon(startIcon)
            )
            // 종료 마커
            googleMap?.addMarker(
                MarkerOptions()
                    .position(routeCoords.last())
                    .icon(endIcon)
            )

            // 카메라 이동
            val cameraUpdate = CameraUpdateFactory.newLatLng(routeCoords.first())
            googleMap?.moveCamera(cameraUpdate)

            // 거리 계산
            val totalDistanceKm = calculateTotalDistance(routeCoords)
            val kmTv: TextView = view?.findViewById(R.id.km_tv) ?: return
            kmTv.text = String.format("%.1f km", totalDistanceKm)
        }
    }

    private fun getToken(): String? {
        return activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getString("token", null)
    }

    // [수정됨] Naver LatLng -> Google LatLng
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

    // Bitmap을 Base64 문자열로 변환하는 함수
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun bitmapToMultipart(bitmap: Bitmap): MultipartBody.Part {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()

        val requestBody = byteArray.toRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("walkReviewImages", "image.jpg", requestBody)
    }

    // [추가됨] MapFragment.kt와 동일한 마커 아이콘 변환 헬퍼 함수
    private fun bitmapDescriptorFromVector(context: Context, @DrawableRes vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            draw(canvas)
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}