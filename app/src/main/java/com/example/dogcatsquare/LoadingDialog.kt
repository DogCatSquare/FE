package com.example.dogcatsquare

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.widget.TextView
import com.example.dogcatsquare.databinding.DialogLoadingBinding

class LoadingDialog(context: Context) : Dialog(context) {
    private lateinit var binding: DialogLoadingBinding
    private val handler = Handler(Looper.getMainLooper())
    private var currentDots = 0
    private val loadingTexts = arrayOf("로딩중", "로딩중.", "로딩중..", "로딩중...")

    private val updateLoadingText = object : Runnable {
        override fun run() {
            if (isShowing) {  // 다이얼로그가 표시 중일 때만 실행
                binding.loadingTextView.text = loadingTexts[currentDots]
                currentDots = (currentDots + 1) % loadingTexts.size
                handler.postDelayed(this, 500)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCanceledOnTouchOutside(false)
    }

    override fun show() {
        super.show()
        // show 될 때마다 애니메이션 새로 시작
        currentDots = 0
        startLoadingAnimation()
    }

    private fun startLoadingAnimation() {
        handler.removeCallbacks(updateLoadingText)  // 기존 콜백 제거
        handler.post(updateLoadingText)  // 새로운 애니메이션 시작
    }

    override fun dismiss() {
        handler.removeCallbacks(updateLoadingText)
        super.dismiss()
    }
}