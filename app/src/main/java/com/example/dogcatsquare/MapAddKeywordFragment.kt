package com.example.dogcatsquare

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dogcatsquare.databinding.FragmentMapAddKeywordBinding

class MapAddKeywordFragment : Fragment() {
    private var _binding: FragmentMapAddKeywordBinding? = null
    private val binding get() = _binding!!

    private var placeId: Int = 0
    private var placeName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            placeId = it.getInt("placeId")
            placeName = it.getString("placeName", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapAddKeywordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupBackButton()
    }

    private fun setupUI() {
        binding.placeName.text = "${placeName}의"
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(placeId: Int, placeName: String) = MapAddKeywordFragment().apply {
            arguments = Bundle().apply {
                putInt("placeId", placeId)
                putString("placeName", placeName)
            }
        }
    }
}