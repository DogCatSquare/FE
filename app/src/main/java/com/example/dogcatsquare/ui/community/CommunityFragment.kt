package com.example.dogcatsquare.ui.community

import PostApiService
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.dogcatsquare.R
import com.example.dogcatsquare.api.RetrofitClient
import com.example.dogcatsquare.data.api.BoardApiService
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.community.GetMyBoardHomeResponse
import com.example.dogcatsquare.data.community.MyBoardResponse
import com.example.dogcatsquare.data.community.MyBoardResult
import com.example.dogcatsquare.data.model.mypage.GetUserResponse
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.FragmentCommunityBinding
import com.example.dogcatsquare.ui.community.BoardSettingsActivity
import com.google.android.material.tabs.TabLayoutMediator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommunityFragment : Fragment() {

    private lateinit var binding: FragmentCommunityBinding
    private lateinit var pagerAdapter: CommunityPagerAdapter
    private val myBoardNames = mutableListOf<MyBoardResult>() // 마이게시판 이름 리스트

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setInfo()

        // 초기 어댑터 설정 (빈 리스트)
        pagerAdapter = CommunityPagerAdapter(this, emptyList(), emptyList())
        binding.viewPager.adapter = pagerAdapter

        // 마이게시판 API 호출하여 동적 데이터 반영
        getMyBoardHome()

        // TabLayout의 텍스트 색상 및 선택된 색상 설정
        binding.tabLayout.setTabTextColors(Color.parseColor("#000000"), Color.parseColor("#FFB200"))
        binding.tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FFB200"))
        binding.tabLayout.tabRippleColor = null

        // 톱니바퀴 클릭 이벤트 설정
        binding.ivSettings.setOnClickListener {
            val intent = Intent(requireContext(), BoardSettingsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val token = getToken()
        if (token != null) {
            getMyBoardHome()
        }
    }

    // 상단바 소개 설정 -> 추후 뷰모델로 수정
    private fun setInfo() {
        val token = getToken()
        val getMyInfoService = RetrofitObj.getRetrofit().create(UserRetrofitItf::class.java)
        getMyInfoService.getUser("Bearer $token").enqueue(object : Callback<GetUserResponse> {
            override fun onResponse(call: Call<GetUserResponse>, response: Response<GetUserResponse>) {
                Log.d("RETROFIT/SUCCESS", response.toString())
                val resp: GetUserResponse = response.body()!!
                if (resp != null){
                    if(resp.isSuccess){ // 응답 성공 시

                        Log.d("MYPAGE/SUCCESS", response.toString())

                        // UI 적용
                        binding.tvNickname.text = resp.result.nickname
                        Glide.with(this@CommunityFragment)
                            .load(resp.result.profileImageUrl)
                            .signature(ObjectKey(System.currentTimeMillis().toString())) // 캐시 무효화
                            .placeholder(R.drawable.ic_profile_default)
                            .into(binding.ivProfile)
                        binding.tvBreed.text = resp.result.firstPetBreed
                        binding.tvLocation.text = "${resp.result.si} " + "${resp.result.gu}"

                    } else {
                        Log.e("MYPAGE/FAILURE", "응답 코드: ${resp.code}, 응답메시지: ${resp.message}")
                    }
                } else {
                    Log.d("MYPAGE/FAILURE", "Response body is null")
                }
            }

            override fun onFailure(call: Call<GetUserResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }

        })
    }

    private fun getMyBoardHome() {
        val token = getToken()
        RetrofitObj.getRetrofit().create(BoardApiService::class.java).getMyBoards("Bearer $token")
            .enqueue(object : Callback<MyBoardResponse> {
                override fun onResponse(
                    call: Call<MyBoardResponse>,
                    response: Response<MyBoardResponse>
                ) {
                    if (response.isSuccessful) {
                        myBoardNames.clear()
                        myBoardNames.addAll(response.body()?.result ?: emptyList())

                        // UI 업데이트
                        setupViewPagerAndTabs()
                    }
                }

                override fun onFailure(call: Call<MyBoardResponse>, t: Throwable) {
                    Log.d("RETROFIT/FAILURE", t.message.toString())
                }
            })
    }

    private fun setupViewPagerAndTabs() {
        val defaultFragments = listOf(
            CommunityHomeFragment() // home
        )

        val defaultTabTitles = listOf("홈")

        // 마이게시판 프래그먼트 추가
        val myBoardFragments = myBoardNames.map { board  ->
            MyBoardFragment.newInstance(board.boardName, board.boardId) // 마이게시판 프래그먼트 생성
        }

        val myBoardTitles = myBoardNames.map { it.boardName }

        // 새로운 데이터로 어댑터 업데이트
        pagerAdapter.updateFragments(defaultFragments + myBoardFragments, defaultTabTitles + myBoardTitles)

        // TabLayout과 ViewPager2 연결
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = pagerAdapter.getTitle(position)
        }.attach()

        // TabLayout의 텍스트 색상 및 선택된 색상 설정
        binding.tabLayout.setTabTextColors(Color.parseColor("#000000"), Color.parseColor("#FFB200"))
        binding.tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FFB200"))
    }

    private fun getToken(): String? {
        val sharedPref = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        return sharedPref.getString("token", null)
    }
}
