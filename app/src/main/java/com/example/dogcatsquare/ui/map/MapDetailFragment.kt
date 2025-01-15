package com.example.dogcatsquare.ui.map

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.DetailImg
import com.example.dogcatsquare.DetailImgRVAdapter
import com.example.dogcatsquare.MapPrice
import com.example.dogcatsquare.MapPriceRVAdapter
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentMapDetailBinding

class MapDetailFragment : Fragment() {
    private var _binding: FragmentMapDetailBinding? = null
    private var imgDatas = ArrayList<DetailImg>()
    private var priceDatas = ArrayList<MapPrice>()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton()
        setupRecyclerView()

        // Bundle에서 데이터 받아오기
        arguments?.let { args ->
            val placeName = args.getString("placeName")
            val placeType = args.getString("placeType")
            val placeDistance = args.getString("placeDistance")
            val placeLocation = args.getString("placeLocation")
            val placeCall = args.getString("placeCall")
            val placeChar1 = args.getString("placeChar1")
            val placeImg = args.getInt("placeImg")

            // 받아온 데이터를 뷰에 설정
            binding.placeName.text = placeName
            binding.placeType.text = placeType
            binding.placeLocation.text = placeLocation?.split(" ")?.getOrNull(2) ?: ""
            binding.placeDistance.text = placeDistance
            binding.placeCall.text = placeCall
            binding.placeLocationFull.text = placeLocation
            binding.placeChar1.text = placeChar1

        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            // 이전 Fragment로 돌아가기
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        imgDatas.apply {
            add(DetailImg(R.drawable.ic_place_img_default))
            add(DetailImg(R.drawable.ic_place_img_default))
            add(DetailImg(R.drawable.ic_place_img_default))
            add(DetailImg(R.drawable.ic_place_img_default))
            add(DetailImg(R.drawable.ic_place_img_default))
            add(DetailImg(R.drawable.ic_place_img_default))
        }

        val detailImgRVAdapter = DetailImgRVAdapter(imgDatas)
        binding.detailImgRV.apply {
            adapter = detailImgRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        priceDatas.apply {
            add(MapPrice("강아지 건강검진", "30,000원"))
            add(MapPrice("고양이 건강검진", "30,000원"))
            add(MapPrice("초진비", "30,000원"))
            add(MapPrice("재진비", "30,000원"))
        }

        val mapPriceRVAdapter = MapPriceRVAdapter(priceDatas)
        binding.mapPriceRV.apply {
            adapter = mapPriceRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}