package com.example.dogcatsquare.ui.mypage

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.dogcatsquare.R
import com.example.dogcatsquare.RetrofitObj
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.mypage.GetUserResponse
import com.example.dogcatsquare.databinding.FragmentMypageBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MypageFragment : Fragment() {
    lateinit var binding: FragmentMypageBinding

    private fun getToken(): String? {
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

    private fun getUserId(): Int {
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("userId", -1)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMypageBinding.inflate(inflater, container, false)

        // 내 정보 수정
        binding.goEditInfoIv.setOnClickListener {
            // Fragment 전환
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, EditInfoFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        // 내 커뮤니티 모아보기
        binding.goMyCommunityIv.setOnClickListener {
            // Fragment 전환
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MyCommunityFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        // 내 후기 모아보기
        binding.goMyReviewIv.setOnClickListener {
            // Fragment 전환
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, MyReviewFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }

        // 공지사항
        binding.goAnnouncementIv.setOnClickListener {

        }

        // 회원탈퇴 클릭
        binding.deleteTv.setOnClickListener {
            showDeleteDialog()
        }

        // 로그아웃 버튼 클릭
        binding.logoutBtn.setOnClickListener {

        }

        return binding.root
    }

    private fun showDeleteDialog() {
        val dialog = CustomDeleteDialog(requireContext())

        dialog.setItemClickListener(object : CustomDeleteDialog.ItemClickListener{
            override fun onClick(message: String) {
                Toast.makeText(requireContext(), "${message}", Toast.LENGTH_SHORT).show()
            }
        })

        dialog.show()
    }

    // onResume 메서드는 프래그먼트가 사용자와 상호작용을 재개할 때 호출 됨. 즉, 마이페이지 조회 시 최신 정보를 불러옴
    override fun onResume() {
        super.onResume()
        val token = getToken()
        val userId = getUserId()
        if (userId != -1 && token != null) {
            fetchMyProfile() // 마이페이지 조회 API 연동 함수 호출
        }
    }

    private fun fetchMyProfile(){
        val BEARER_TOKEN = getToken()

        // 회원정보 조회 API 연동
        val authService = RetrofitObj.getRetrofit().create(UserRetrofitItf::class.java)
        authService.getUser("Bearer $BEARER_TOKEN").enqueue(object: Callback<GetUserResponse> {
            override fun onResponse(
                call: Call<GetUserResponse>,
                response: Response<GetUserResponse>
            ) {
                Log.d("RETROFIT/SUCCESS", response.toString())
                val resp: GetUserResponse = response.body()!!
                if (resp != null){
                    if(resp.isSuccess){ // 응답 성공 시

                        Log.d("MYPAGE/SUCCESS", response.toString())

                        // 응답 값 확인
                        Log.d("nickname", resp.result.nickname)
                        Log.d("email", resp.result.email)
                        Log.d("profileUrl", resp.result.profileImageUrl)

                        // UI 적용
                        binding.nicknameTv.text = resp.result.nickname
                        Glide.with(requireContext())
                            .load(resp.result.profileImageUrl)
                            .into(binding.profileIv)

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
}