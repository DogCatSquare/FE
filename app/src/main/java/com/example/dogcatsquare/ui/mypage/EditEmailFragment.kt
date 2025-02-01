package com.example.dogcatsquare.ui.mypage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.dogcatsquare.R
import com.example.dogcatsquare.RetrofitObj
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.login.CheckEmailResponse
import com.example.dogcatsquare.databinding.FragmentEditEmailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditEmailFragment : Fragment() {
    lateinit var binding: FragmentEditEmailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditEmailBinding.inflate(inflater, container, false)

        // 이메일 인증 추후 구현


        binding.emailCheckBtn.setOnClickListener {
            val email = binding.emailEt.text.toString()
            if (isEmailUsed(email)) { // 이미 사용 중인 이메일
                binding.signupEmailCheckTv.text = "이미 사용 중인 이메일입니다"
                binding.signupEmailCheckTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            }
            else {
                binding.signupEmailCheckTv.text = "사용 가능한 이메일입니다"
                binding.signupEmailCheckTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color1))
//                email_check = true

                binding.textView41.visibility = View.VISIBLE
                binding.verifyEmailEt.visibility = View.VISIBLE
                binding.verifyEmailBtn.visibility = View.VISIBLE
            }
        }
        binding.emailEt.addTextChangedListener {
            validateEmail()
        }
        checkEmail()

        return binding.root
    }

    // 이메일 체크 -> 인증
    private fun validateEmail() {
        val email = binding.emailEt.text.toString()
        val emailCheckTv = binding.signupEmailCheckTv

        // 이메일 정규식
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$".toRegex()

        if (!email.matches(emailRegex)) {
            emailCheckTv.text = "유효하지 않은 이메일 형식입니다"
            emailCheckTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            binding.emailCheckBtn.isClickable = false
        } else {
            emailCheckTv.text = ""
            emailCheckTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color1))
            binding.emailCheckBtn.isClickable = true
        }
    }

    // 이메일 중복 체크
    private fun isEmailUsed(email: String): Boolean {
        var checkEmail: Boolean = false
        val checkEmailService = RetrofitObj.getRetrofit().create(UserRetrofitItf::class.java)
        checkEmailService.checkEmail(email).enqueue(object : Callback<CheckEmailResponse> {
            override fun onResponse(
                call: Call<CheckEmailResponse>,
                response: Response<CheckEmailResponse>
            ) {
                Log.d("CheckEmail/SUCCESS", response.toString())

                when(response.code()) {
                    200 -> {
                        val resp: CheckEmailResponse = response.body()!!
                        if (resp != null) {
                            if (resp.isSuccess) {
                                if (resp.result == false) { // 일치하는 닉네임 없음 -> 중복 x
                                    checkEmail = true
                                    Log.d("CheckEmail/SUCCESS", checkEmail.toString())
                                } else { // 일치하는 닉네임 있음 -> 중복 o
                                    checkEmail = false
                                    Log.d("CheckEmail/SUCCESS", checkEmail.toString())
                                }
                            } else {
                                Log.e("CheckEmail/FAILURE", "응답 코드: ${resp.code}, 응답 메시지: ${resp.message}")
                            }
                        } else {
                            Log.d("CheckEmail/FAILURE", "Response body is null")
                            Log.e("CheckEmail/FAILURE", "응답 코드: ${resp.code}, 응답메시지: ${resp.message}")
                        }
                    }
                }
            }

            override fun onFailure(call: Call<CheckEmailResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }

        })

        return checkEmail
    }

    // 이메일 인증
    private fun verifyEmail() {
        binding.emailCheckBtn.setOnClickListener {

        }
    }

    private fun checkEmail() {
        binding.verifyEmailBtn.setOnClickListener {
            val email = binding.emailEt.text.toString()

            if (email.isNotEmpty()) {
                parentFragmentManager.setFragmentResult(
                    "emailResult", // 고유한 키
                    Bundle().apply { putString("email", email) }
                )
            }

            // 현재 프래그먼트 종료하고 이전 화면으로 돌아가기
            parentFragmentManager.popBackStack()
        }
    }
}