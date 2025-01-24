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

    private val userPolyline = PolylineOverlay()
    private val coords = mutableListOf<LatLng>()
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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

        val nextButton: Button = rootView.findViewById(R.id.end_walk_bt)
        nextButton.setOnClickListener {
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

    private fun initPolyLine(startLatLng: LatLng) {
        // 초기 위치를 두 번 추가하여 최소 두 개의 좌표를 보장
        coords.add(startLatLng)
        coords.add(startLatLng)

        // coords 리스트의 크기가 2 이상일 때만 setCoords 호출
        if (coords.size >= 2) {
            userPolyline.coords = coords
            userPolyline.color = Color.DKGRAY
            userPolyline.map = naverMap
        }
    }

    private fun updateCoords(latLng: LatLng) {
        coords.add(latLng)
        // coords 리스트의 크기가 2 이상일 때만 setCoords 호출
        if (coords.size >= 2) {
            userPolyline.coords = coords
        }
    }

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
                initPolyLine(currentLatLng)
                val cameraUpdate = CameraUpdate.scrollTo(currentLatLng)
                naverMap.moveCamera(cameraUpdate)
            }
        }
    }

    override fun sendLocation(latitude: Double, longitude: Double) {
        Log.d("MAIN_LOCATION", "$latitude, $longitude")
        updateCoords(LatLng(latitude, longitude)) // 새로운 좌표로 경로 업데이트
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
        val intent = Intent(requireContext(), LocationService::class.java)
        requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindLocationService() {
        if (isBound) {
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
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}
