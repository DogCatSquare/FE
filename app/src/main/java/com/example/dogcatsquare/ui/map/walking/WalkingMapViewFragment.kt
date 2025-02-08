package com.example.dogcatsquare.ui.map.walking

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.dogcatsquare.LocationService
import com.example.dogcatsquare.data.api.LocationUpdateInterface
import com.example.dogcatsquare.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.PolylineOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.google.android.gms.location.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage

class WalkingMapViewFragment : Fragment(), OnMapReadyCallback, LocationUpdateInterface {

    private var locationService: LocationService? = null
    private var isBound = false

    private val userPolyline = PolylineOverlay()
    private val coords = mutableListOf<LatLng>()
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private var isTrackingLocation = false  // 위치 추적 상태 추가

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_mapwalking_mapview, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupLocationRequest()

        if (hasPermission()) {
            checkAndRequestGPS()
            bindLocationService()
        } else {
            requestLocationPermission()
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                childFragmentManager.beginTransaction().replace(R.id.map_fragment, it).commit()
            }

        mapFragment.getMapAsync(this)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        // 산책 시작 버튼 클릭 시 위치 추적 시작
        val startButton: Button = rootView.findViewById(R.id.start_walk_bt)
        startButton.setOnClickListener {
            Toast.makeText(requireContext(), "산책 시작", Toast.LENGTH_SHORT).show()
            setInitialMarker()
            startWalk()  // 위치 추적 시작
        }

        // 산책 종료 버튼 클릭 시 화면 전환
        val nextButton: Button = rootView.findViewById(R.id.end_walk_bt)
        nextButton.setOnClickListener {
            Toast.makeText(requireContext(), "산책 종료", Toast.LENGTH_SHORT).show()
            endMarker()

            // 3초 후에 화면 전환
            Handler(Looper.getMainLooper()).postDelayed({
                val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.main_frm, WalkingReviewFragment())
                transaction.addToBackStack(null)
                transaction.commit()
            }, 3000)
        }

        return rootView
    }

    private fun startWalk() {
        if (!isTrackingLocation) {
            // 위치 추적 시작
            isTrackingLocation = true
            requestLocationUpdate() // 위치 업데이트 요청
        }
    }

    private fun requestLocationUpdate() {
        if (hasPermission()) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
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
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult.locations.forEach { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                updateCoords(latLng)  // 위치 갱신
            }
        }
    }

    private fun updateCoords(latLng: LatLng) {
        if (coords.isNotEmpty()) {
            val lastLatLng = coords.last()
            val distance = FloatArray(1)
            Location.distanceBetween(lastLatLng.latitude, lastLatLng.longitude, latLng.latitude, latLng.longitude, distance)

            if (distance[0] < 5) return
        }

        coords.add(latLng)
        if (coords.size >= 2) {
            userPolyline.coords = coords
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        if (!hasPermission()) return

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
            val currentLatLng = if (location != null) {
                LatLng(location.latitude, location.longitude)
            } else {
                LatLng(37.5665, 126.9780)
            }

            initPolyLine(currentLatLng)
            val cameraUpdate = CameraUpdate.scrollTo(currentLatLng)
            naverMap.moveCamera(cameraUpdate)
        }
    }

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

    // 경로 라인 초기화
    private fun initPolyLine(startLatLng: LatLng) {
        coords.add(startLatLng)
        coords.add(startLatLng)

        if (coords.size >= 2) {
            userPolyline.coords = coords
            userPolyline.width = 10
            userPolyline.color = Color.parseColor("#FFB200")
            userPolyline.map = naverMap
        }
    }

    // 위치 업데이트 설정
    private fun setupLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 2000 // 2초마다 위치 업데이트 (기존 1초보다 배터리 절약)
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    // 시작 위치 마커
    private fun setInitialMarker() {
        val startMarker = Marker()
        startMarker.icon = OverlayImage.fromResource(R.drawable.ic_start_marker)
        startMarker.position = LatLng(
            naverMap.cameraPosition.target.latitude,
            naverMap.cameraPosition.target.longitude
        )
        startMarker.map = naverMap
    }

    // 끝 위치 마커
    private fun endMarker(){
        val endMarker = Marker()
        endMarker.icon = OverlayImage.fromResource(R.drawable.ic_end_marker)
        endMarker.position = LatLng(
            naverMap.cameraPosition.target.latitude,
            naverMap.cameraPosition.target.longitude
        )
        endMarker.map = naverMap
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
