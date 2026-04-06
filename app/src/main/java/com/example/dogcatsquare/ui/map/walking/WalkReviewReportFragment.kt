package com.example.dogcatsquare.ui.map.walking

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.network.RetrofitClient
import com.example.dogcatsquare.data.model.walk.ReportRequest
import com.example.dogcatsquare.databinding.FragmentMapReportBinding
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalkReviewReportFragment : Fragment() {
    private var _binding: FragmentMapReportBinding? = null
    private val binding get() = _binding!!

    private var walkId: Int = -1
    private var reviewId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        walkId = arguments?.getInt("walkId", -1) ?: -1
        reviewId = arguments?.getInt("reviewId", -1) ?: -1

        setupBackButton()
        setupRadioGroup()
        setupOtherReasonEditText()
        setupReportButton()
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupRadioGroup() {
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ),
            intArrayOf(
                Color.GRAY,
                Color.parseColor("#FFB200")
            )
        )

        binding.apply {
            rgReasons.apply {
                rbAdvertising.buttonTintList = colorStateList
                rbHateSpeech.buttonTintList = colorStateList
                rbObsceneContent.buttonTintList = colorStateList
                rbSpam.buttonTintList = colorStateList
                rbPersonalInfo.buttonTintList = colorStateList
                rbOther.buttonTintList = colorStateList
            }
        }

        binding.etOtherReason.isEnabled = false
        binding.etOtherReason.alpha = 0.5f

        binding.rgReasons.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbOther -> {
                    binding.etOtherReason.isEnabled = true
                    binding.etOtherReason.alpha = 1.0f
                    binding.reportButton.setImageResource(R.drawable.bt_deactivated_complete)
                }
                -1 -> {
                    binding.etOtherReason.isEnabled = false
                    binding.etOtherReason.alpha = 0.5f
                    binding.reportButton.setImageResource(R.drawable.bt_deactivated_complete)
                }
                else -> {
                    binding.etOtherReason.isEnabled = false
                    binding.etOtherReason.alpha = 0.5f
                    binding.etOtherReason.setText("")
                    binding.reportButton.setImageResource(R.drawable.bt_activated_complete)
                }
            }
        }
    }

    private fun setupOtherReasonEditText() {
        binding.etOtherReason.addTextWatcher(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.rgReasons.checkedRadioButtonId == R.id.rbOther) {
                    val length = s?.length ?: 0
                    binding.reportButton.setImageResource(
                        // 0자여도 안 되는데 기획에는 "50자 제한"이라고 되어있고 Other이면 사유 적는다고 되어있음. MapReportFragment에서는 10자. 일단 기존대로.
                        if (length >= 10) R.drawable.bt_activated_complete
                        else R.drawable.bt_deactivated_complete
                    )
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupReportButton() {
        binding.reportButton.setOnClickListener {
            if (!isValidInput()) {
                Toast.makeText(context, "신고 사유를 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val reportType = when (binding.rgReasons.checkedRadioButtonId) {
                R.id.rbAdvertising -> "ADVERTISEMENT"
                R.id.rbHateSpeech -> "ABUSE_HATE_SPEECH"
                R.id.rbObsceneContent -> "ADULT_CONTENT"
                R.id.rbSpam -> "SPAM"
                R.id.rbPersonalInfo -> "PERSONAL_INFO"
                R.id.rbOther -> "OTHER"
                else -> return@setOnClickListener
            }

            val otherReason = if (reportType == "OTHER") {
                binding.etOtherReason.getText()
            } else {
                null
            }

            submitReport(reportType, otherReason)
        }
    }

    private fun isValidInput(): Boolean {
        val checkedId = binding.rgReasons.checkedRadioButtonId
        if (checkedId == -1) return false

        if (checkedId == R.id.rbOther) {
            return binding.etOtherReason.getText().isNotEmpty() // 10자 이상 체크는 이미 되어있으나 안전하게
        }

        return true
    }

    private fun submitReport(reportType: String, otherReason: String?) {
        lifecycleScope.launch {
            try {
                val token = getToken()
                if (token == null) {
                    Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val request = ReportRequest(
                    reportType = reportType,
                    otherReason = otherReason
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.walkingApiService.reportWalkReview(
                        token = "Bearer $token",
                        walkId = walkId,
                        reviewId = reviewId,
                        body = request
                    )
                }

                if (response.isSuccess) {
                    Toast.makeText(context, "후기 신고가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    val errorMessage = response.message ?: "신고 처리 중 오류가 발생했습니다."
                    if (errorMessage.contains("이미 신고")) {
                        Toast.makeText(context, "이미 신고한 산책로입니다.", Toast.LENGTH_SHORT).show()
                    } else if (errorMessage.contains("자신의 후기")) {
                        Toast.makeText(context, "자신의 후기는 신고할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    } else if (!response.isSuccess && response.message?.contains("정지") == true) {
                        Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun getToken(): String? {
        return activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getString("token", null)
    }

    private fun handleError(e: Exception) {
        if (e is retrofit2.HttpException) {
            try {
                val errorBody = e.response()?.errorBody()?.string()
                if (errorBody != null) {
                    val errorResponse = Gson().fromJson(errorBody, com.example.dogcatsquare.data.model.map.BaseResponse::class.java)
                    if (errorResponse?.message != null) {
                        if (errorResponse.message.contains("이미 신고")) {
                            Toast.makeText(context, "이미 신고한 산책로입니다.", Toast.LENGTH_SHORT).show()
                            return
                        } else if (errorResponse.message.contains("정지")) {
                            Toast.makeText(context, errorResponse.message, Toast.LENGTH_SHORT).show()
                            return
                        } else if (errorResponse.message.contains("자신의 후기")) {
                            Toast.makeText(context, "자신의 후기는 신고할 수 없습니다.", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                }
            } catch (ex: Exception) {
                // Ignore parsing errors
            }
            
            val errorMessage = when (e.code()) {
                400 -> "잘못된 요청입니다."
                401 -> "로그인이 필요합니다."
                403 -> "권한이 없습니다."
                404 -> "존재하지 않는 리뷰입니다."
                else -> "서버 오류가 발생했습니다. (${e.code()})"
            }
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "알 수 없는 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(walkId: Int, reviewId: Int) = WalkReviewReportFragment().apply {
            arguments = Bundle().apply {
                putInt("walkId", walkId)
                putInt("reviewId", reviewId)
            }
        }
    }
}
