package com.example.dogcatsquare.ui.mypage

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.model.login.CheckEmailResponse
import com.example.dogcatsquare.data.model.login.SendVerficationRequest
import com.example.dogcatsquare.data.model.login.SendVerficationResponse
import com.example.dogcatsquare.data.model.login.VerifyRequest
import com.example.dogcatsquare.data.model.login.VerifyResponse
import com.example.dogcatsquare.databinding.FragmentEditEmailBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditEmailFragment : Fragment() {
    lateinit var binding: FragmentEditEmailBinding

    private var countDownTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditEmailBinding.inflate(inflater, container, false)

        binding.emailCheckBtn.setOnClickListener {
            val email = binding.emailEt.text.toString()
            if (isEmailUsed(email)) { // 이미 사용 중인 이메일
                binding.signupEmailCheckTv.text = "이미 사용 중인 이메일입니다"
                binding.signupEmailCheckTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
            }
            else {
                binding.signupEmailCheckTv.text = "사용 가능한 이메일입니다"
                binding.signupEmailCheckTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.main_color1))

                binding.textView41.visibility = View.VISIBLE
                binding.verifyEmailEt.visibility = View.VISIBLE
                binding.verifyEmailBtn.visibility = View.VISIBLE
                binding.verifyResultTv.visibility = View.VISIBLE

                sendEmail(email)
            }
        }

        binding.emailEt.addTextChangedListener {
            validateEmail()
        }

        binding.verifyBtn.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val code = binding.verifyEmailEt.text.toString()
            verifyEmail(email, code)
        }

        checkEmail()

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

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
    private fun sendEmail(email: String) {
        val sendEmailService = RetrofitObj.getRetrofit().create(UserRetrofitItf::class.java)
        sendEmailService.sendVerification(SendVerficationRequest(email)).enqueue(object : Callback<SendVerficationResponse> {
            override fun onResponse(call: Call<SendVerficationResponse>, response: Response<SendVerficationResponse>) {
                Log.d("SendEmailResult", response.toString())
                Toast.makeText(requireContext(), "인증 코드가 발송되었습니다.", Toast.LENGTH_SHORT).show()
                startTimer()
            }

            override fun onFailure(call: Call<SendVerficationResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }

        })
    }

    private fun verifyEmail(email: String, code: String) {
        val verifyEmailService = RetrofitObj.getRetrofit().create(UserRetrofitItf::class.java)
        verifyEmailService.verifyEmail(VerifyRequest(email, code)).enqueue(object : Callback<VerifyResponse> {
            override fun onResponse(call: Call<VerifyResponse>, response: Response<VerifyResponse>) {
                val resp: VerifyResponse = response.body()!!
                if (resp != null) {
                    if (resp.verified) {
                        binding.verifyEmailCheckTv.text = "이메일 인증이 완료되었습니다"
                    } else {
                        binding.verifyEmailCheckTv.text = "이메일 인증을 다시 진행해주세요"
                    }
                } else {
                    Log.d("CheckEmail/FAILURE", "Response body is null")
                }
            }

            override fun onFailure(call: Call<VerifyResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }
        })
    }

    private fun startTimer() {
        countDownTimer?.cancel() // 이전 타이머가 있으면 취소
        val totalTime = 5 * 60 * 1000L // 5분 = 300,000ms

        countDownTimer = object : CountDownTimer(totalTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                binding.verifyEmailTimeTv.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                binding.verifyEmailTimeTv.text = "00:00"
                Toast.makeText(requireContext(), "인증 시간이 만료되었습니다. 다시 요청하세요.", Toast.LENGTH_SHORT).show()
                binding.verifyEmailTimeTv.isEnabled = false // 인증 버튼 비활성화
            }
        }.start()

        binding.verifyEmailBtn.isEnabled = true // 타이머 시작 시 인증 버튼 활성화
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