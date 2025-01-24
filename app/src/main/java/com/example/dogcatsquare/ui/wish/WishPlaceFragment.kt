package com.example.dogcatsquare.ui.wish

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.MapButton
import com.example.dogcatsquare.MapButtonRVAdapter
import com.example.dogcatsquare.R
import com.example.dogcatsquare.WishPlace
import com.example.dogcatsquare.WishPlaceRVAdapter
import com.example.dogcatsquare.databinding.FragmentWishPlaceBinding

class WishPlaceFragment : Fragment() {
    private var _binding: FragmentWishPlaceBinding? = null
    private val binding get() = _binding!!

    private val buttonDatas by lazy { ArrayList<MapButton>() }
    private val placeDatas by lazy { ArrayList<WishPlace>() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        buttonDatas.clear()
        placeDatas.clear()

        buttonDatas.apply {
            add(MapButton("전체"))
            add(MapButton("병원"))
            add(MapButton("호텔"))
            add(MapButton("공원"))
            add(MapButton("카페"))
            add(MapButton("기타"))
        }

        val mapButtonRVAdapter = MapButtonRVAdapter(buttonDatas)
        binding.mapButtonRV.apply {
            adapter = mapButtonRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        placeDatas.apply {
            add(WishPlace("ABC 고양이 호텔", "호텔", "0.55km", "서울시 양천구 중앙로 222 3층", "02-1234-5678", "중성화 수술", R.drawable.ic_place_img_default, "리뷰(18)"))
            add(WishPlace("ABC 고양이 호텔", "호텔", "0.55km", "서울시 양천구 중앙로 222 3층", "02-1234-5678", "중성화 수술", R.drawable.ic_place_img_default, "리뷰(18)"))
            add(WishPlace("ABC 고양이 호텔", "호텔", "0.55km", "서울시 양천구 중앙로 222 3층", "02-1234-5678", "중성화 수술", R.drawable.ic_place_img_default, "리뷰(18)"))
        }

        val wishPlaceRVAdapter = WishPlaceRVAdapter(placeDatas)
        binding.wishPlaceRV.apply {
            adapter = wishPlaceRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}