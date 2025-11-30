package com.example.dogcatsquare

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.dogcatsquare.data.api.SseAlarmService
import com.example.dogcatsquare.databinding.ActivityMainBinding
import com.example.dogcatsquare.ui.community.CommunityFragment
import com.example.dogcatsquare.ui.home.HomeFragment
import com.example.dogcatsquare.ui.map.location.MapFragment
import com.example.dogcatsquare.ui.mypage.MypageFragment
import com.example.dogcatsquare.ui.wish.WishFragment

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val locationViewModel: LocationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상단바 색깔
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        initBottomNavigation()
    }

    // 바텀네비게이션 아이콘 선택 시 각 프래그먼트로 이동하는 함수
    private fun initBottomNavigation(){
        // 기본은 홈프래그먼트
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frm, HomeFragment())
            .commitAllowingStateLoss()

        binding.mainBnv.itemIconTintList = null

        binding.mainBnv.setOnItemSelectedListener{ item ->
            when (item.itemId) {

                R.id.homeFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, HomeFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }

                R.id.mapFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, MapFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
                R.id.communityFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, CommunityFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
                R.id.wishFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, WishFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
                R.id.mypageFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, MypageFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
            }
            false
        }

        fun startAlarmService() {
            val serviceIntent = Intent(this, SseAlarmService::class.java)

            // API 레벨 26(Oreo) 이상에서는 Context.startService() 대신 startForegroundService()를 사용해야 함
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
    }
}