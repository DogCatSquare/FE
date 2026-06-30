package com.example.dogcatsquare.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentTermsBinding

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

        when (type) {
            TYPE_SERVICE -> binding.contentTv.setText(R.string.service_terms)
            TYPE_PRIVACY -> binding.contentTv.setText(R.string.privacy_policy)
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