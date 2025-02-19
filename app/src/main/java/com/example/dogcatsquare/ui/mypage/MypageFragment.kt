package com.example.dogcatsquare.ui.mypage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.model.login.DeleteUserResponse
import com.example.dogcatsquare.data.model.mypage.GetUserResponse
import com.example.dogcatsquare.databinding.FragmentMypageBinding
import com.example.dogcatsquare.ui.login.LoginDetailActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MypageFragment : Fragment() {
    lateinit var binding: FragmentMypageBinding

    var name: String = ""
    var phone: String = ""
    var profileImg: String = ""

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
            val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val editor = sharedPref?.edit()

            editor?.clear()
            editor?.commit()

            val intent = Intent(requireContext(), LoginDetailActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            requireActivity().finish()
        }

        // 내 정보 수정
        binding.goEditInfoIv.setOnClickListener {
            val token = getToken()
            if (token != null) {
                fetchProfileAndNavigate(token)
            }
        }

        return binding.root
    }

    private fun showDeleteDialog() {
        val dialog = CustomDeleteDialog(requireContext())

        dialog.setItemClickListener(object : CustomDeleteDialog.ItemClickListener{
            override fun onClick() {
                val token = getToken()

                val deleteUserService = RetrofitObj.getRetrofit().create(UserRetrofitItf::class.java)
                deleteUserService.deleteUser("Bearer $token").enqueue(object : Callback<DeleteUserResponse> {
                    override fun onResponse(
                        call: Call<DeleteUserResponse>,
                        response: Response<DeleteUserResponse>
                    ) {
                        if(response.isSuccessful) {
                            navigateToLogin()
                        } else {
                            Toast.makeText(context, "회원 탈퇴에 실패했습니다", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<DeleteUserResponse>, t: Throwable) {
                        Log.d("RETROFIT/FAILURE", t.message.toString())
                    }

                })
            }
        })

        dialog.show()
    }

    private fun navigateToLogin() {
        val intent = Intent(context, LoginDetailActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // 모든 액티비티 삭제 후 이동
        startActivity(intent)
    }

        // onResume 메서드는 프래그먼트가 사용자와 상호작용을 재개할 때 호출 됨. 즉, 마이페이지 조회 시 최신 정보를 불러옴
    override fun onResume() {
        super.onResume()
        val token = getToken()
        val userId = getUserId()

        if (userId != -1 && token != null) {
            fetchMyProfile(token) // 마이페이지 조회 API 연동 함수 호출
        }
    }

    private fun fetchMyProfile(token: String){
        val BEARER_TOKEN = token

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

                        // UI 적용
                        binding.nicknameTv.text = resp.result.nickname
                        Glide.with(this@MypageFragment)
                            .load(resp.result.profileImageUrl)
                            .signature(ObjectKey(System.currentTimeMillis().toString())) // 캐시 무효화
                            .placeholder(R.drawable.ic_profile_default)
                            .into(binding.profileIv)

                        name = resp.result.nickname
                        phone = resp.result.phoneNumber
                        profileImg = resp.result.profileImageUrl.toString()

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

    private fun fetchProfileAndNavigate(token: String) {
        val authService = RetrofitObj.getRetrofit().create(UserRetrofitItf::class.java)
        authService.getUser("Bearer $token").enqueue(object : Callback<GetUserResponse> {
            override fun onResponse(call: Call<GetUserResponse>, response: Response<GetUserResponse>) {
                val resp = response.body()
                if (resp != null && resp.isSuccess) {
                    val fragment = EditInfoFragment().apply {
                        arguments = Bundle().apply {
                            putString("nickname", resp.result.nickname)
                            putString("phone", resp.result.phoneNumber)
                            putString("profileImg", resp.result.profileImageUrl)
                        }
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, fragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
                }
            }

            override fun onFailure(call: Call<GetUserResponse>, t: Throwable) {
                Log.e("FETCH/FAILURE", t.message.toString())
            }
        })
    }
}