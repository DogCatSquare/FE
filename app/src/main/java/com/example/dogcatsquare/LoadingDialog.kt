package com.example.dogcatsquare

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import com.example.dogcatsquare.databinding.DialogLoadingBinding

class LoadingDialog(context: Context) : Dialog(context) {
    private lateinit var binding: DialogLoadingBinding
    private val handler = Handler(Looper.getMainLooper())
    private var currentDots = 0
    private val loadingTexts = arrayOf("로딩중.", "로딩중..", "로딩중...")

    private val updateLoadingText = object : Runnable {
        override fun run() {
            binding.loadingTextView.text = loadingTexts[currentDots]
            currentDots = (currentDots + 1) % loadingTexts.size
            handler.postDelayed(this, 500) // 0.5초마다 텍스트 업데이트
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 다이얼로그 배경을 투명하게 설정
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 다이얼로그 영역 밖 터치 시 다이얼로그가 닫히지 않도록 설정
        setCanceledOnTouchOutside(false)

        // 로딩 텍스트 애니메이션 시작
        startLoadingAnimation()
    }

    private fun startLoadingAnimation() {
        handler.post(updateLoadingText)
    }

    override fun dismiss() {
        handler.removeCallbacks(updateLoadingText)
        super.dismiss()
    }
}