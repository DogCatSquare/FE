package com.example.dogcatsquare.ui.map.walking

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap // [추가]
import android.graphics.Canvas // [추가]
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.DrawableRes // [추가]
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.dogcatsquare.LocationService
import com.example.dogcatsquare.data.api.LocationUpdateInterface
import com.example.dogcatsquare.R

// [수정] Google Map Import
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
// ---

class WalkingMapViewFragment : Fragment(), OnMapReadyCallback, LocationUpdateInterface {

    private var locationService: LocationService? = null
    private var isBound = false

    // [수정] Google Polyline
    private var userPolyline: Polyline? = null
    // [수정] PolylineOptions를 클래스 멤버로 미리 정의 (Race Condition 방지)
    private val userPolylineOptions = PolylineOptions()
        .width(10f)
        .color(Color.parseColor("#FFB200"))

    // [수정] Google LatLng
    private val coords = mutableListOf<LatLng>()
    // [수정] GoogleMap
    private var googleMap: GoogleMap? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private var isTrackingLocation = false  // 위치 추적 상태

    // 타이머 관련 변수
    private var startTime: Long = 0L
    private var elapsedTime: Long = 0L
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isTrackingLocation) {
                elapsedTime = System.currentTimeMillis() - startTime
                timerHandler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // [수정] fragment_mapwalking_mapview.xml을 사용
        val rootView = inflater.inflate(R.layout.fragment_mapwalking_mapview, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupLocationRequest()

        if (hasPermission()) {
            checkAndRequestGPS()
            bindLocationService()
        } else {
            requestLocationPermission()
        }

        // [수정] Google SupportMapFragment
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction().replace(R.id.map_fragment, it).commit()
            }
        mapFragment.getMapAsync(this)

        // 산책 시작 버튼
        val startButton: Button = rootView.findViewById(R.id.start_walk_bt)
        startButton.setOnClickListener {
            // [수정] 맵이 준비되었는지 확인
            if (googleMap == null) {
                Toast.makeText(requireContext(), "지도를 불러오는 중입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(requireContext(), "산책 시작", Toast.LENGTH_SHORT).show()
            setInitialMarker()
            startWalk()
        }

        // 산책 종료 버튼
        val nextButton: Button = rootView.findViewById(R.id.end_walk_bt)
        nextButton.setOnClickListener {
            if (!isTrackingLocation) {
                Toast.makeText(requireContext(), "산책을 먼저 시작해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(requireContext(), "산책 종료", Toast.LENGTH_SHORT).show()
            endMarker()
            stopWalk()

            // 3초 후에 다음 Fragment로 전환
            Handler(Looper.getMainLooper()).postDelayed({
                val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
                val walkingReviewFragment = WalkingReviewFragment()
                val bundle = Bundle()

                val minutes = elapsedTime / 1000 / 60
                bundle.putLong("elapsedTime", minutes)

                // [수정] Google LatLng 리스트를 전달
                val coordsList = ArrayList<LatLng>(coords)
                bundle.putParcelableArrayList("routeCoords", coordsList)

                walkingReviewFragment.arguments = bundle
                transaction.replace(R.id.main_frm, walkingReviewFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }, 3000)
        }

        return rootView
    }

    private fun startWalk() {
        if (!isTrackingLocation) {
            isTrackingLocation = true
            startTime = System.currentTimeMillis()
            timerHandler.postDelayed(timerRunnable, 1000)
            requestLocationUpdate()
        }
    }

    private fun stopWalk() {
        if (isTrackingLocation) {
            isTrackingLocation = false
            timerHandler.removeCallbacks(timerRunnable)
            elapsedTime = System.currentTimeMillis() - startTime
            stopLocationUpdate()
        }
    }

    private fun requestLocationUpdate() {
        if (hasPermission()) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            requestLocationPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isBound && hasPermission()) {
            checkAndRequestGPS()
            bindLocationService()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isTrackingLocation) {
            stopLocationUpdate()
        }
        unbindLocationService()
    }

    private fun stopLocationUpdate() {
        if (isTrackingLocation) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            isTrackingLocation = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isTrackingLocation) {
            stopLocationUpdate()
        }
        unbindLocationService()
        googleMap = null // [추가] 맵 리소스 해제
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult.locations.forEach { location ->
                // [수정] Google LatLng
                val latLng = LatLng(location.latitude, location.longitude)
                updateCoords(latLng)
            }
        }
    }

    // [수정] Google LatLng 및 Polyline 업데이트 로직
    private fun updateCoords(latLng: LatLng) {
        if (googleMap == null) return // 맵이 준비되지 않으면 아무것도 하지 않음

        if (coords.isNotEmpty()) {
            val lastLatLng = coords.last()
            val distance = FloatArray(1)
            Location.distanceBetween(
                lastLatLng.latitude, lastLatLng.longitude,
                latLng.latitude, latLng.longitude, distance
            )
            if (distance[0] < 5) return
        }
        coords.add(latLng)

        // [수정] Google Polyline 업데이트
        if (coords.size >= 2) {
            if (userPolyline != null) {
                // 맵에 Polyline이 이미 그려져 있다면, points 목록만 갱신
                userPolyline?.points = coords
            } else {
                // 맵에 Polyline이 아직 없다면 (initPolyLine이 아직 호출 안된 극단적 경우)
                // PolylineOptions에 점을 추가하고 맵에 새로 그림
                userPolylineOptions.addAll(coords) // <-- .points()가 아니라 .addAll()입니다.
                userPolyline = googleMap?.addPolyline(userPolylineOptions)
            }
        }
    }

    // [수정] onMapReady (Google)
    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        googleMap?.uiSettings?.isMyLocationButtonEnabled = true

        if (!hasPermission()) {
            val seoul = LatLng(37.5665, 126.9780)
            initPolyLine(seoul) // Polyline 초기화
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 16f))
            return
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        googleMap?.isMyLocationEnabled = true // 내 위치 파란 점 표시

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            val currentLatLng = if (location != null) {
                LatLng(location.latitude, location.longitude)
            } else {
                LatLng(37.5665, 126.9780) // 기본 위치 (서울)
            }
            initPolyLine(currentLatLng)
            // [수정] Google CameraUpdate
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f)
            googleMap?.moveCamera(cameraUpdate)
        }
    }

    // [수정] Google LatLng
    override fun sendLocation(latitude: Double, longitude: Double) {
        Log.d("MAIN_LOCATION", "$latitude, $longitude")
        updateCoords(LatLng(latitude, longitude))
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        ActivityCompat.requestPermissions(requireActivity(), permissions.toTypedArray(), LOCATION_PERMISSION_REQUEST_CODE)
    }

    private fun checkAndRequestGPS() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private fun bindLocationService() {
        val intent = Intent(requireContext(), LocationService::class.java)
        requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindLocationService() {
        if (isBound) {
            try {
                requireContext().unbindService(connection)
            } catch (e: IllegalArgumentException) {
                Log.e("WalkingMapViewFragment", "Service not registered", e)
            }
            isBound = false
        }
    }

    // [수정] Google LatLng 및 Google Polyline
    private fun initPolyLine(startLatLng: LatLng) {
        coords.clear() // [추가] 좌표 목록 초기화
        coords.add(startLatLng)
        coords.add(startLatLng)
        if (coords.size >= 2) {
            // [수정] userPolylineOptions는 이미 정의됨. addAll로 점만 추가
            userPolylineOptions.addAll(coords)
            userPolyline = googleMap?.addPolyline(userPolylineOptions)
        }
    }

    private fun setupLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 2000 // 2초마다 위치 업데이트
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    // [추가] 마커 아이콘 변환 헬퍼 (WalkingReviewFragment와 동일)
    private fun bitmapDescriptorFromVector(context: Context, @DrawableRes vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            draw(canvas)
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    // [수정] Google Marker
    private fun setInitialMarker() {
        val startIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_start_marker)
        googleMap?.cameraPosition?.target?.let {
            googleMap?.addMarker(
                MarkerOptions()
                    .position(it)
                    .icon(startIcon)
            )
        }
    }

    // [수정] Google Marker
    private fun endMarker() {
        val endIcon = bitmapDescriptorFromVector(requireContext(), R.drawable.ic_end_marker)
        googleMap?.cameraPosition?.target?.let {
            googleMap?.addMarker(
                MarkerOptions()
                    .position(it)
                    .icon(endIcon)
            )
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.getService()
            locationService?.setLocationUpdateInterface(this@WalkingMapViewFragment)
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}