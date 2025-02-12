package com.example.dogcatsquare.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.data.api.UserRetrofitItf
import com.example.dogcatsquare.data.model.login.CheckEmailResponse
import com.example.dogcatsquare.data.model.login.CheckNicknameResponse
import com.example.dogcatsquare.data.model.login.SendVerficationRequest
import com.example.dogcatsquare.data.model.login.SendVerficationResponse
import com.example.dogcatsquare.data.model.login.VerifyRequest
import com.example.dogcatsquare.data.model.login.VerifyResponse
import com.example.dogcatsquare.databinding.ActivitySignupBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignupBinding

    private var countDownTimer: CountDownTimer? = null

    private var nickname_check: Boolean = false
    private var email_check: Boolean = false
    private var email_verify_check: Boolean = false
    private var pw_check: Boolean = false
    private var phone_check: Boolean = false
    private var checkbox_check: Boolean = false
    private var adAgree: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상단바 색깔
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        // 배경화면 클릭 시 키보드 숨기기
        binding.signupCl.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN){
                currentFocus?.let { view ->
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    view.clearFocus()
                }
            }
            false // 터치 이벤트를 소비하지 않음
        }

        binding.nicknameCheckBtn.setOnClickListener {
            val nickname = binding.nicknameEt.text.toString()
            if (!isNicknameUsed(nickname)) {
                binding.signupNicknameCheckTv.text = "사용 가능한 닉네임입니다"
                binding.signupNicknameCheckTv.setTextColor(ContextCompat.getColor(this, R.color.main_color1))
                nickname_check = true
            }
            else {
                binding.signupNicknameCheckTv.text = "이미 사용 중인 닉네임입니다"
                binding.signupNicknameCheckTv.setTextColor(ContextCompat.getColor(this, R.color.red))
            }
        }

        binding.emailCheckBtn.setOnClickListener {
            val email = binding.emailEt.text.toString()
            if (isEmailUsed(email)) { // 이미 사용 중인 이메일
                binding.signupEmailCheckTv.text = "이미 사용 중인 이메일입니다"
                binding.signupEmailCheckTv.setTextColor(ContextCompat.getColor(this, R.color.red))
            }
            else {
                binding.signupEmailCheckTv.text = "사용 가능한 이메일입니다"
                binding.signupEmailCheckTv.setTextColor(ContextCompat.getColor(this, R.color.main_color1))

                binding.textView41.visibility = View.VISIBLE
                binding.verifyEmailEt.visibility = View.VISIBLE
                binding.verifyEmailBtn.visibility = View.VISIBLE
                binding.verifyEmailTimeTv.visibility = View.VISIBLE
                binding.verifyEmailCheckTv.visibility = View.VISIBLE

                sendEmail(email)
                startTimer()
            }
        }

        binding.verifyEmailBtn.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val code = binding.verifyEmailEt.text.toString()
            verifyEmail(email, code)
        }

        setupValidation()

        // 다음 버튼 클릭 시 바텀 시트 뜸
        binding.signupNextBtn.setOnClickListener {
            showBottomSheetDialog()
        }
    }

    // 약관동의 바텀시트
    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)

        // 레이아웃 인플레이트
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_signup, null)
        bottomSheetDialog.setContentView(view)

        // 체크박스 초기화
        val checkBoxAll = view.findViewById<CheckBox>(R.id.checkBox_all)
        val checkBox1 = view.findViewById<CheckBox>(R.id.checkBox1)
        val checkBox2 = view.findViewById<CheckBox>(R.id.checkBox2)
        val checkBox3 = view.findViewById<CheckBox>(R.id.checkBox3)
        val doneButton = view.findViewById<Button>(R.id.signup_done_btn)

        // 버튼 초기 상태 비활성화
        updateButtonState(checkBox1, checkBox2, doneButton)

        // 약관 전체 동의 체크박스 클릭 시 전체 체크 박스 ON
        checkBoxAll.setOnCheckedChangeListener { _, isChecked ->
            checkBox1.isChecked = isChecked
            checkBox2.isChecked = isChecked
            checkBox3.isChecked = isChecked
            updateButtonState(checkBox1, checkBox2, doneButton)
        }

        // 각 개별 체크박스 클릭 리스너
        val individualCheckBoxes = listOf(checkBox1, checkBox2, checkBox3)
        individualCheckBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, _ ->
                // 전체 동의 체크박스 업데이트
                checkBoxAll.isChecked = individualCheckBoxes.all { it.isChecked }

                // 완료 버튼 활성화 상태 업데이트
                updateButtonState(checkBox1, checkBox2, doneButton)
            }
        }

        // 완료 버튼 클릭 이벤트
        doneButton.setOnClickListener {
            adAgree = true
            checkSignup()
        }

        // 바텀시트 다이얼로그 표시
        bottomSheetDialog.show()
    }

    // 버튼 상태 업데이트 함수
    private fun updateButtonState(checkBox1: CheckBox, checkBox2: CheckBox, doneButton: Button) {
        // 필수 체크박스 모두 체크되었는지 확인
        val isMandatoryChecked = checkBox1.isChecked && checkBox2.isChecked

        // 버튼 활성화/비활성화 상태 설정
        doneButton.isEnabled = isMandatoryChecked
        if (isMandatoryChecked) {
            doneButton.setBackgroundColor(ContextCompat.getColor(this, R.color.main_color1)) // 활성화 색상
            doneButton.setTextColor(ContextCompat.getColor(this, R.color.white)) // 텍스트 색상
            checkbox_check = true
        } else {
            doneButton.setBackgroundColor(ContextCompat.getColor(this, R.color.gray1)) // 비활성화 색상
            doneButton.setTextColor(ContextCompat.getColor(this, R.color.gray4)) // 텍스트 색상
        }
    }

    // 닉네임 체크
    private fun validateNickname() {
        val nickname = binding.nicknameEt.text.toString()
        val nicknameCheckTv = binding.signupNicknameCheckTv

        // 정규식: 한글, 영어 숫자 구성, 최대 10자
        val nicknameRegex = "^[a-zA-Zㄱ-힣0-9]{1,10}$".toRegex()

        if (!nickname.matches(nicknameRegex)) {
            nicknameCheckTv.text = "한글, 영어 최대 10자"
            nicknameCheckTv.setTextColor(ContextCompat.getColor(this, R.color.red))
            binding.nicknameCheckBtn.isClickable = false
            nickname_check = false
        } else {
            nicknameCheckTv.text = ""
            nicknameCheckTv.setTextColor(ContextCompat.getColor(this, R.color.main_color1))
            binding.nicknameCheckBtn.isClickable = true
        }
    }

    // 닉네임 중복 체크
    private fun isNicknameUsed(nickname: String): Boolean {
        var checkNickname: Boolean = false
        val checkNicknameService = RetrofitObj.getRetrofit().create(UserRetrofitItf::class.java)
        checkNicknameService.checkNickname(nickname).enqueue(object : Callback<CheckNicknameResponse>{
            override fun onResponse(
                call: Call<CheckNicknameResponse>,
                response: Response<CheckNicknameResponse>
            ) {
                Log.d("CheckNickname/SUCCESS", response.toString())

                when(response.code()) {
                    200 -> {
                        val resp: CheckNicknameResponse = response.body()!!
                        if (resp != null) {
                            if (resp.isSuccess) {
                                if (resp.result == false) { // 일치하는 닉네임 없음 -> 중복 x
                                    checkNickname = true
                                    Log.d("CheckNickname/SUCCESS", checkNickname.toString())
                                } else { // 일치하는 닉네임 있음 -> 중복 o
                                    checkNickname = false
                                    Log.d("CheckNickname/SUCCESS", checkNickname.toString())
                                }
                            } else {
                                Log.e("CheckNickname/FAILURE", "응답 코드: ${resp.code}, 응답 메시지: ${resp.message}")
                            }
                        } else {
                            Log.d("CheckNickname/FAILURE", "Response body is null")
                            Log.e("CheckNickname/FAILURE", "응답 코드: ${resp.code}, 응답메시지: ${resp.message}")
                        }
                    }
                }
            }

            override fun onFailure(call: Call<CheckNicknameResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }

        })

        return checkNickname
    }

    // 이메일 체크 -> 인증
    private fun validateEmail() {
        val email = binding.emailEt.text.toString()
        val emailCheckTv = binding.signupEmailCheckTv

        // 이메일 정규식
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$".toRegex()

        if (!email.matches(emailRegex)) {
            emailCheckTv.text = "유효하지 않은 이메일 형식입니다"
            emailCheckTv.setTextColor(ContextCompat.getColor(this, R.color.red))
            binding.emailCheckBtn.isClickable = false
            email_check = false
        } else {
            emailCheckTv.text = ""
            emailCheckTv.setTextColor(ContextCompat.getColor(this, R.color.main_color1))
            binding.emailCheckBtn.isClickable = true
        }
    }

    // 이메일 중복 체크
    private fun isEmailUsed(email: String): Boolean {
        var checkEmail: Boolean = false
        val checkEmailService = RetrofitObj.getRetrofit().create(UserRetrofitItf::class.java)
        checkEmailService.checkEmail(email).enqueue(object : Callback<CheckEmailResponse>{
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
                Toast.makeText(this@SignupActivity, "인증 코드가 발송되었습니다.", Toast.LENGTH_SHORT).show()
//                startTimer()
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
                        binding.verifyEmailCheckTv.setTextColor(ContextCompat.getColor(this@SignupActivity, R.color.main_color1))

                        countDownTimer?.cancel() // 타이머 중지
                        binding.verifyEmailTimeTv.visibility = View.GONE // 타이머 숨기기
                        email_verify_check = true
                    } else {
                        binding.verifyEmailCheckTv.text = "이메일 인증을 다시 진행해주세요"
                        binding.verifyEmailCheckTv.setTextColor(ContextCompat.getColor(this@SignupActivity, R.color.red))
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
        countDownTimer?.cancel() // 기존 타이머가 있으면 취소
        val totalTime = 5 * 60 * 1000L // 5분 (300,000ms)

        // 타이머 텍스트 초기화
        binding.verifyEmailTimeTv.text = "05:00"
        binding.verifyEmailTimeTv.visibility = View.VISIBLE

        countDownTimer = object : CountDownTimer(totalTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60

                // UI 스레드에서 강제로 업데이트
                runOnUiThread {
                    binding.verifyEmailTimeTv.text = String.format("%02d:%02d", minutes, seconds)
                }
            }

            override fun onFinish() {
                runOnUiThread {
                    binding.verifyEmailTimeTv.text = "00:00"
                    binding.verifyEmailCheckTv.text = "인증 시간이 만료되었습니다"
                    binding.verifyEmailBtn.isEnabled = false // 인증 버튼 비활성화
                }
            }
        }.start()

        binding.verifyEmailBtn.isEnabled = true // 타이머 시작 시 인증 버튼 활성화
    }


    // 비밀번호 체크
    private fun validatePassword() {
        val password = binding.pwEt.text.toString()
        val passwordCheck = binding.pwCheckEt.text.toString()
        val passwordCheckTv = binding.signupPwCheckTv

        // 정규식: 소문자, 숫자 포함 8~15자
        val passwordRegex = "^(?=.*[a-z])(?=.*\\d)[a-zA-Z\\d]{8,15}$".toRegex()

        if (!password.matches(passwordRegex)) {
            passwordCheckTv.text = "소문자, 숫자 필수 포함 8~15자"
            passwordCheckTv.setTextColor(ContextCompat.getColor(this, R.color.red))
        } else if (password != passwordCheck) { // 비밀번호 확인 불일치
            passwordCheckTv.text = "비밀번호가 불일치합니다"
            passwordCheckTv.setTextColor(ContextCompat.getColor(this, R.color.red))
        } else {
            passwordCheckTv.text = "비밀번호가 일치합니다"
            passwordCheckTv.setTextColor(ContextCompat.getColor(this, R.color.main_color1))
            pw_check = true
        }
    }

    // 전화번호 체크
    private fun validatephone() {
        val phone = binding.phoneEt.text.toString()
        val phoneRegex ="^01[0-9]{8}$".toRegex()

        if (phone.matches(phoneRegex)) {
            phone_check = true
        }
    }

    private fun setupValidation() {
        binding.nicknameEt.addTextChangedListener {
            validateNickname()
        }

        binding.emailEt.addTextChangedListener {
            validateEmail()
        }

        binding.pwEt.addTextChangedListener {
            validatePassword()
        }

        binding.pwCheckEt.addTextChangedListener {
            validatePassword()
        }

        binding.phoneEt.addTextChangedListener {
            validatephone()
        }
    }


    private fun checkSignup() {
        // && email_verify_check 추가
        if (nickname_check && pw_check && checkbox_check && phone_check && adAgree && email_verify_check) {
            val bundle = Bundle().apply {
                putString("nickname", binding.nicknameEt.text.toString()) // 닉네임
                putString("email", binding.emailEt.text.toString())       // 이메일
                putString("password", binding.pwCheckEt.text.toString()) // 비밀번호
                putString("phoneNumber", binding.phoneEt.text.toString()) // 전화번호
                putBoolean("adAgree", adAgree)
            }

            val intent = Intent(this, SignupPetInfoActivity::class.java).apply {
                putExtras(bundle) // Bundle을 Intent에 추가
            }

            startActivity(intent)
        }
    }
}