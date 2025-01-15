package com.example.dogcatsquare.ui.community

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.ActivityCommunityWriteBinding

class CommunityWriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommunityWriteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommunityWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // EditText와 Button 설정
        val etTitle = binding.etTitle
        val etContent = binding.etContent
        val btnNext = binding.btnNext

        // TextChangedListener를 통해 '다음' 버튼 활성화 관리
        etTitle.addTextChangedListener { updateButtonState() }
        etContent.addTextChangedListener { updateButtonState() }

        // '다음' 버튼 클릭 이벤트 처리
        btnNext.setOnClickListener {
            // TODO: 다음 단계로 이동하는 로직 추가
        }
    }

    // '다음' 버튼 활성화 상태 업데이트
    private fun updateButtonState() {
        val isTitleFilled = binding.etTitle.text.isNotBlank()
        val isContentFilled = binding.etContent.text.isNotBlank()

        binding.btnNext.isEnabled = isTitleFilled && isContentFilled
        binding.btnNext.setBackgroundResource(
            if (binding.btnNext.isEnabled) R.drawable.bg_button_enabled else R.drawable.bg_button_disabled
        )
    }
}
