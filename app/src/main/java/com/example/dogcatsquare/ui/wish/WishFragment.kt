package com.example.dogcatsquare.ui.wish

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.dogcatsquare.databinding.FragmentWishBinding

class WishFragment : Fragment() {
    private var _binding: FragmentWishBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishBinding.inflate(inflater, container, false)

        // 기본적으로 WishPlaceFragment를 표시
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(binding.wishFrm.id, WishPlaceFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()
        }

        binding.placeButton.setOnClickListener {
            childFragmentManager.beginTransaction()
                .replace(binding.wishFrm.id, WishPlaceFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()
        }

        binding.walkButton.setOnClickListener {
            childFragmentManager.beginTransaction()
                .replace(binding.wishFrm.id, WishWalkFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}