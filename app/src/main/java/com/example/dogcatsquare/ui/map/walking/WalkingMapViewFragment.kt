package com.example.dogcatsquare.ui.map.walking

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.dogcatsquare.LocationService
import com.example.dogcatsquare.LocationUpdateInterface
import com.example.dogcatsquare.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.PolylineOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class WalkingMapViewFragment : Fragment(), OnMapReadyCallback, LocationUpdateInterface {

    private var locationService: LocationService? = null
    private var isBound = false

    private val userPolyline = PolylineOverlay()  // 사용자 경로를 그릴 폴리라인 오버레이
    private val coords = mutableListOf<LatLng>()  // 사용자 경로 좌표 리스트
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isTracking = false  // 경로 추적이 시작되었는지 여부

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_mapwalking_mapview, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (!hasPermission()) {
            requestLocationPermission()
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().replace(R.id.map_fragment, it).commit()
            }

        mapFragment.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        bindLocationService()

        val startButton: Button = rootView.findViewById(R.id.start_walk_bt)
        startButton.setOnClickListener {
            if (!isTracking) {
                startTracking()  // 경로 추적 시작
            }
            Toast.makeText(requireContext(), "산책 시작", Toast.LENGTH_SHORT).show()
        }

        val endButton: Button = rootView.findViewById(R.id.end_walk_bt)
        endButton.setOnClickListener {
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.main_frm, WalkingReviewFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return rootView
    }

    override fun onResume() {
        super.onResume()
        if (!isBound) {
            bindLocationService()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isBound) {
            unbindLocationService()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unbindLocationService()
    }

    // 폴리라인 초기화 함수
    private fun initPolyLine(startLatLng: LatLng) {
        Log.d("WalkingMap", "initPolyLine called with: $startLatLng")
        coords.add(startLatLng)
        coords.add(startLatLng)

        if (coords.size >= 2) {
            userPolyline.coords = coords
            userPolyline.color = Color.DKGRAY
            userPolyline.map = naverMap
        }
    }

    // 좌표 업데이트 함수
    private fun updateCoords(latLng: LatLng) {
        Log.d("WalkingMap", "updateCoords called with: $latLng")
        coords.add(latLng)
        if (coords.size >= 2) {
            userPolyline.coords = coords
        }
    }

    // 맵이 준비되었을 때 호출되는 함수
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                Log.d("WalkingMap", "Current location: $currentLatLng")
                if (!isTracking) {
                    initPolyLine(currentLatLng)  // 경로 초기화
                    val cameraUpdate = CameraUpdate.scrollTo(currentLatLng)
                    naverMap.moveCamera(cameraUpdate)
                }
            }
        }
    }

    // 위치 서비스에서 좌표를 받아서 경로 업데이트
    override fun sendLocation(latitude: Double, longitude: Double) {
        Log.d("WalkingMap", "sendLocation called with latitude: $latitude, longitude: $longitude")
        if (isTracking) {
            Log.d("MAIN_LOCATION", "$latitude, $longitude")
            updateCoords(LatLng(latitude, longitude))  // 경로 업데이트
        } else {
            Log.d("WalkingMap", "Tracking is not enabled. sendLocation not updating.")
        }
    }

    // 경로 추적 시작
    private fun startTracking() {
        isTracking = true
        Log.d("WalkingMap", "경로 추적 시작")
        // 경로 추적 시작 시 초기화 및 상태 설정
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
        )
    }

    private fun bindLocationService() {
        Log.d("WalkingMap", "Binding location service...")
        val intent = Intent(requireContext(), LocationService::class.java)
        requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindLocationService() {
        if (isBound) {
            Log.d("WalkingMap", "Unbinding location service...")
            requireContext().unbindService(connection)
            isBound = false
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.getService()
            locationService?.setLocationUpdateInterface(this@WalkingMapViewFragment)
            isBound = true
            Log.d("WalkingMap", "Location service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            Log.d("WalkingMap", "Location service disconnected")
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}
