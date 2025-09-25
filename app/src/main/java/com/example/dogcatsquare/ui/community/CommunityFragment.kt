package com.example.dogcatsquare.ui.community

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    private lateinit var pagerAdapter: CommunityPagerAdapter
    private var mediator: TabLayoutMediator? = null

    // 진행 중 네트워크 콜 → 화면 파괴 시 취소
    private var getUserCall: Call<GetUserResponse>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
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
        // 미디에이터 분리
        mediator?.detach()
        mediator = null

        // 진행 중 콜 취소
        getUserCall?.cancel()
        getUserCall = null

        // 바인딩 정리
        _binding = null
    }

    private fun setInfo() {
        val ctx = context ?: return
        val token = getTokenSafe() ?: return

        val svc = RetrofitObj.getRetrofit(ctx).create(UserRetrofitItf::class.java)

        getUserCall = svc.getUser("Bearer $token")
        getUserCall?.enqueue(object : Callback<GetUserResponse> {
            override fun onResponse(
                call: Call<GetUserResponse>,
                response: Response<GetUserResponse>
            ) {
                // 화면이 분리/파괴되었으면 종료
                if (!isAdded || view == null ||
                    viewLifecycleOwner.lifecycle.currentState.isAtLeast(
                        androidx.lifecycle.Lifecycle.State.STARTED
                    ).not()
                ) return

                val resp = response.body()
                if (response.isSuccessful && resp?.isSuccess == true) {
                    val r = resp.result

                    // 널/빈 문자열 안전 처리
                    binding.tvNickname.text = r.nickname ?: ""
                    binding.tvBreed.text = r.firstPetBreed ?: ""
                    val si = r.si ?: ""
                    val gu = r.gu ?: ""
                    binding.tvLocation.text = "$si $gu".trim()

                    // Glide는 Fragment 레퍼런스로 생명주기 안전
                    Glide.with(this@CommunityFragment)
                        .load(r.profileImageUrl)
                        .apply(RequestOptions.circleCropTransform())
                        // 캐시 무효화가 꼭 필요하지 않다면 주석 처리 가능
                        .signature(ObjectKey(System.currentTimeMillis().toString()))
                        .placeholder(R.drawable.ic_profile_default)
                        .error(R.drawable.ic_profile_default)
                        .into(binding.ivProfile)
                } else {
                    Log.w("MYPAGE", "getUser fail code=${response.code()} body=$resp")
                }
            }

            override fun onFailure(call: Call<GetUserResponse>, t: Throwable) {
                // 화면 분리 시 콜백 들어와도 안전 종료
                if (!isAdded) return
                Log.e("MYPAGE", "getUser error", t)
            }
        })
    }

    private fun getTokenSafe(): String? {
        val ctx = context ?: return null
        val sp = ctx.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
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