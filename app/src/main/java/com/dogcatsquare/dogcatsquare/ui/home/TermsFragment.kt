package com.dogcatsquare.dogcatsquare.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.dogcatsquare.dogcatsquare.databinding.FragmentTermsBinding

class TermsFragment : Fragment() {

    private var _binding: FragmentTermsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTermsBinding.inflate(inflater, container, false)

        val title = arguments?.getString(ARG_TITLE) ?: ""
        val type = arguments?.getString(ARG_TYPE) ?: ""

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
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_TYPE = "type"

        const val TYPE_SERVICE = "service"
        const val TYPE_PRIVACY = "privacy"

        fun newInstance(title: String, type: String): TermsFragment {
            return TermsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_TYPE, type)
                }
            }
        }
    }
}