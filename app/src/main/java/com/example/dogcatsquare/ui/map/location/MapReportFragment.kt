package com.example.dogcatsquare.ui.map.location

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
import com.example.dogcatsquare.RetrofitClient
import com.example.dogcatsquare.data.map.PlaceReviewReportRequest
import com.example.dogcatsquare.databinding.FragmentMapReportBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapReportFragment : Fragment() {
    private var _binding: FragmentMapReportBinding? = null
    private val binding get() = _binding!!

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

        // 라디오 버튼들에 색상 적용
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

        // CustomEditText 초기 상태 설정 (처음에는 비활성화)
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
                    // 다른 라디오 버튼 선택 시
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
                R.id.rbHateSpeech -> "HATE"
                R.id.rbObsceneContent -> "OBSCENITY"
                R.id.rbSpam -> "SPAM"
                R.id.rbPersonalInfo -> "PERSONAL_INFO"
                R.id.rbOther -> "OTHER"
                else -> return@setOnClickListener
            }

            val otherReason = if (reportType == "OTHER") binding.etOtherReason.getText() else null

            submitReport(reportType, otherReason)
        }
    }

    private fun isValidInput(): Boolean {
        val checkedId = binding.rgReasons.checkedRadioButtonId
        if (checkedId == -1) return false

        if (checkedId == R.id.rbOther) {
            return binding.etOtherReason.getText().length >= 10
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

                val request = PlaceReviewReportRequest(
                    reportType = reportType,
                    otherReason = otherReason
                )

                Log.d("MapReport", "Sending report request: $request")

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.placesApiService.reportReview(
                        token = "Bearer $token",
                        placeReviewId = reviewId,
                        request = request
                    )
                }

                Log.d("MapReport", "Received response: $response")

                if (response.isSuccess) {
                    Toast.makeText(context, "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    val errorMessage = when (response.code) {
                        "REVIEW_NOT_FOUND" -> "존재하지 않는 리뷰입니다."
                        "ALREADY_REPORTED" -> "이미 신고한 리뷰입니다."
                        "INVALID_REPORT_TYPE" -> "올바르지 않은 신고 유형입니다."
                        else -> response.message ?: "신고 처리 중 오류가 발생했습니다."
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MapReport", "Error submitting report", e)
                handleError(e)
            }
        }
    }

    private fun getToken(): String? {
        return activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ?.getString("token", null)
    }

    private fun handleError(e: Exception) {
        val errorMessage = when (e) {
            is retrofit2.HttpException -> {
                when (e.code()) {
                    400 -> "잘못된 요청입니다."
                    401 -> "로그인이 필요합니다."
                    403 -> "권한이 없습니다."
                    404 -> "존재하지 않는 리뷰입니다."
                    500 -> "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                    else -> "서버 오류가 발생했습니다. (${e.code()})"
                }
            }
            is java.net.UnknownHostException -> "서버에 연결할 수 없습니다. 인터넷 연결을 확인해주세요."
            is java.net.SocketTimeoutException -> "서버 응답 시간이 초과되었습니다. 잠시 후 다시 시도해주세요."
            is java.io.IOException -> "네트워크 오류가 발생했습니다. 다시 시도해주세요."
            else -> "알 수 없는 오류가 발생했습니다: ${e.message}"
        }
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(reviewId: Int) = MapReportFragment().apply {
            arguments = Bundle().apply {
                putInt("reviewId", reviewId)
            }
        }
    }
}