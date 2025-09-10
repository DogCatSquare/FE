package com.example.dogcatsquare.ui.community

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.model.mypage.GetUserResponse
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.FragmentCommunityBinding
import com.google.android.material.tabs.TabLayoutMediator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CommunityFragment : Fragment() {

    private lateinit var binding: FragmentCommunityBinding
    private lateinit var pagerAdapter: CommunityPagerAdapter
    private var mediator: TabLayoutMediator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 상단 프로필 영역
        setInfo()

        // 1) 고정 탭 구성
        val titles = listOf(
            "홈",
            "자유 게시판",
            "정보 공유 게시판",
            "질문/상담 게시판",
            "입양/임보 게시판",
            "실종/목격 게시판"
        )

        val fragments = listOf(
            CommunityHomeFragment(),
            MyBoardFragment.newInstance("자유 게시판",       BoardTypes.FREE.id),
            MyBoardFragment.newInstance("정보 공유 게시판", BoardTypes.INFO.id),
            MyBoardFragment.newInstance("질문/상담 게시판", BoardTypes.QNA.id),
            MyBoardFragment.newInstance("입양/임보 게시판", BoardTypes.ADOPT.id),
            MyBoardFragment.newInstance("실종/목격 게시판", BoardTypes.MISSING.id),
        )

        // 2) 어댑터/미디에이터 연결
        pagerAdapter = CommunityPagerAdapter(this, fragments, titles)
        binding.viewPager.adapter = pagerAdapter
        mediator = TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.also { it.attach() }

        // 3) 탭 스타일
        binding.tabLayout.setTabTextColors(Color.parseColor("#000000"), Color.parseColor("#FFB200"))
        binding.tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FFB200"))
        binding.tabLayout.tabRippleColor = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediator?.detach()
    }

    private fun setInfo() {
        val token = getToken() ?: return
        RetrofitObj.getRetrofit(requireContext())
            .create(UserRetrofitItf::class.java)
            .getUser("Bearer $token")
            .enqueue(object : Callback<GetUserResponse> {
                override fun onResponse(call: Call<GetUserResponse>, response: Response<GetUserResponse>) {
                    val resp = response.body() ?: return
                    if (!resp.isSuccess) return
                    val r = resp.result
                    binding.tvNickname.text = r.nickname
                    Glide.with(requireContext())
                        .load(r.profileImageUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .signature(ObjectKey(System.currentTimeMillis().toString()))
                        .placeholder(R.drawable.ic_profile_default)
                        .into(binding.ivProfile)
                    binding.tvBreed.text = r.firstPetBreed
                    binding.tvLocation.text = "${r.si} ${r.gu}"
                }
                override fun onFailure(call: Call<GetUserResponse>, t: Throwable) {}
            })
    }

    private fun getToken(): String? {
        val sp = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        return sp.getString("token", null)
    }
}


enum class BoardTypes(val id: Int) {
    FREE(1),
    INFO(2),
    QNA(3),
    ADOPT(4),
    MISSING(5)
}