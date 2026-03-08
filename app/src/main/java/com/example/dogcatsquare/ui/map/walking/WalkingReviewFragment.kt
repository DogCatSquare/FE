package com.example.dogcatsquare.ui.map.walking

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dogcatsquare.R
import com.example.dogcatsquare.LoadingDialog
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
    private lateinit var loadingDialog: LoadingDialog

    // 후기 입력 UI
    private lateinit var reviewContentEditText: EditText
    private lateinit var submitReviewButton: Button
    private lateinit var rvSelectedImages: RecyclerView
    private lateinit var imageAdapter: SelectedImageAdapter

    // ViewModel for API 호출
    private lateinit var viewModel: WalkReviewViewModel

    private val PICK_IMAGE_REQUEST = 1001
//    private var selectedBitmap: Bitmap? = null
    private var selectedBitmaps: MutableList<Bitmap> = mutableListOf()

    // 예시 산책로 ID (실제 값으로 초기화 필요)
    private var walkId: Long = 20L

    private var placeName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // [수정 없음] fragment_mapwalking_review.xml을 로드
        return inflater.inflate(R.layout.fragment_mapwalking_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())

        // ViewModel 초기화
        viewModel = ViewModelProvider(requireActivity())[WalkReviewViewModel::class.java]

        view.findViewById<ImageView>(R.id.back_btn).setOnClickListener {
            parentFragmentManager.popBackStack() // 이전 화면으로 돌아가기
        }

        // Bundle에서 경과 시간(분)과 경로 좌표 목록을 받음
        elapsedMinutes = arguments?.getLong("elapsedTime", 0L) ?: 0L
        // [수정됨] Google LatLng 타입으로 받도록 ArrayList 타입 변경
        routeCoords = arguments?.getParcelableArrayList("routeCoords") ?: arrayListOf()

        // 경과 시간 텍스트 업데이트 (예: "30분")
        val minTv: TextView = view.findViewById(R.id.min_tv)
        minTv.text = elapsedMinutes.toString()

        placeName = arguments?.getString("placeName") ?: "알 수 없는 장소"

        val addressTv: TextView = view.findViewById(R.id.address_tv)
        addressTv.text = placeName

        // 1. UI 및 리사이클러뷰 초기화
        rvSelectedImages = view.findViewById(R.id.rv_selected_images)
        reviewContentEditText = view.findViewById(R.id.introduction_tv)
        submitReviewButton = view.findViewById(R.id.Completion_bt)

        val textLengthCounter: TextView = view.findViewById(R.id.text_length_counter)

        reviewContentEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                textLengthCounter.text = "$length/300"
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        imageAdapter = SelectedImageAdapter(selectedBitmaps) { position ->
            selectedBitmaps.removeAt(position)
            updateImageUI()
        }
        rvSelectedImages.adapter = imageAdapter
        rvSelectedImages.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // 2. 갤러리 버튼
        view.findViewById<AppCompatImageButton>(R.id.addImg_bt).setOnClickListener {
            if (selectedBitmaps.size >= 5) {
                Toast.makeText(requireContext(), "사진은 최대 5장까지 가능합니다.", Toast.LENGTH_SHORT).show()
            } else {
                openGallery()
            }
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
            val fragment = WalkingReviewTypeFragment().apply {
                arguments = Bundle().apply {
                    putString("placeName", placeName) // 장소 이름 넘기기
                }
            }
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.main_frm, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        submitReviewButton.setOnClickListener {
            val content = reviewContentEditText.text.toString().trim()

            // 유효성 검사
            if (content.isEmpty()) {
                Toast.makeText(requireContext(), "리뷰는 20자 이상 작성해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedBitmaps.isEmpty()) {
                Toast.makeText(requireContext(), "최소 1장의 사진을 추가해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!loadingDialog.isDialogShowing) {
                loadingDialog.show()
            }

            val token = getToken()
            if (token.isNullOrEmpty()) {
                if (loadingDialog.isDialogShowing) loadingDialog.dismiss()
                Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Multipart 이미지 변환
            val imageParts = bitmapsToMultipart(selectedBitmaps)

            viewModel.tempToken = "Bearer $token"

            // 🌟 API 호출 대신, SharedViewModel에 데이터 임시 저장
            viewModel.tempRouteCoords = this.routeCoords
            viewModel.tempElapsedMinutes = this.elapsedMinutes
            viewModel.tempDistance = calculateTotalDistance(this.routeCoords) // 실제 거리 적용
            viewModel.tempContent = content
            viewModel.tempImages = imageParts
            viewModel.tempPlaceName = this.placeName

            // 데이터 저장 후 다음 화면(TypeFragment)으로 전환
            val fragment = WalkingReviewTypeFragment().apply {
                arguments = Bundle().apply {
                    putString("placeName", placeName)
                }
            }
            if (loadingDialog.isDialogShowing) loadingDialog.dismiss()
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_frm, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val clipData = data.clipData
            if (clipData != null) {
                // 여러 장 선택 처리
                for (i in 0 until clipData.itemCount) {
                    if (selectedBitmaps.size < 5) {
                        val uri = clipData.getItemAt(i).uri
                        val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                        selectedBitmaps.add(bitmap)
                    }
                }
            } else {
                // 한 장 선택 처리
                data.data?.let { uri ->
                    if (selectedBitmaps.size < 5) {
                        val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                        selectedBitmaps.add(bitmap)
                    }
                }
            }
            updateImageUI()
        }
    }

    private fun updateImageUI() {
        imageAdapter.notifyDataSetChanged()
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
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(routeCoords.first(), 15.0f)

            googleMap?.moveCamera(cameraUpdate)

            // 거리 계산
            val totalDistanceKm = calculateTotalDistance(routeCoords)
            val kmTv: TextView = view?.findViewById(R.id.km_tv) ?: return
            kmTv.text = String.format("%.1f", totalDistanceKm)
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

//    private fun bitmapToMultipart(bitmap: Bitmap): MultipartBody.Part {
//        val outputStream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
//        val byteArray = outputStream.toByteArray()
//
//        val requestBody = byteArray.toRequestBody("image/*".toMediaTypeOrNull())
//        return MultipartBody.Part.createFormData("walkReviewImages", "image.jpg", requestBody)
//    }

    private fun bitmapsToMultipart(bitmaps: List<Bitmap>): List<MultipartBody.Part> {
        val parts = mutableListOf<MultipartBody.Part>()

        bitmaps.forEachIndexed { index, bitmap ->
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()
            val requestBody = byteArray.toRequestBody("image/*".toMediaTypeOrNull())

            // "walkReviewImages"는 서버 API의 파라미터 이름과 일치해야 합니다.
            parts.add(MultipartBody.Part.createFormData("walkReviewImages", "image_$index.jpg", requestBody))
        }
        return parts
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

    override fun onStop() {
        super.onStop()
        if (::loadingDialog.isInitialized && loadingDialog.isDialogShowing) {
            loadingDialog.dismiss()
        }
    }
}