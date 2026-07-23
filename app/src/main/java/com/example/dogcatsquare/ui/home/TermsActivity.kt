package com.example.dogcatsquare.ui.home

import android.os.Bundle
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.ActivityTermsBinding

class TermsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTermsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상단바 색깔
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
        val type = intent.getStringExtra(EXTRA_TYPE) ?: ""

        binding.titleTv.text = title

        binding.webView.webViewClient = WebViewClient()

        binding.webView.settings.apply {
            javaScriptEnabled = false
            builtInZoomControls = false
            displayZoomControls = false
            setSupportZoom(false)
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        when (type) {
            TYPE_SERVICE ->
                binding.webView.loadUrl("file:///android_asset/terms.html")

            TYPE_PRIVACY ->
                binding.webView.loadUrl("file:///android_asset/privacy_policy.html")
        }

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_TYPE = "type"

        const val TYPE_SERVICE = "service"
        const val TYPE_PRIVACY = "privacy"
    }
}
