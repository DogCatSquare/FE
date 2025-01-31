package com.example.dogcatsquare.ui.map.location

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.data.map.DetailImg
import com.example.dogcatsquare.data.map.MapPrice
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentMapDetailBinding
import com.example.dogcatsquare.ui.map.SearchFragment
import com.google.android.material.bottomsheet.BottomSheetDialog

class MapDetailFragment : Fragment() {
    private var _binding: FragmentMapDetailBinding? = null
    private val binding get() = _binding!!

    private val imgDatas by lazy { ArrayList<DetailImg>() }
    private val priceDatas by lazy { ArrayList<MapPrice>() }

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

        binding.filter.setOnClickListener {
            showSearchOptions()
        }

        binding.searchBox.setOnClickListener {
            val searchFragment = SearchFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, searchFragment)
                .addToBackStack(null)  // 뒤로 가기를 위해 백스택에 추가
                .commit()
        }

        // Bundle에서 데이터 받아오기
        arguments?.let { args ->
            val placeName = args.getString("placeName")
            val placeType = args.getString("placeType")
            val placeDistance = args.getString("placeDistance")
            val placeLocation = args.getString("placeLocation")
            val placeCall = args.getString("placeCall")
            val char1Text = args.getString("char1Text")
            val char2Text = args.getString("char2Text")
            val char3Text = args.getString("char3Text")
            val placeImg = args.getInt("placeImg")

            // 받아온 데이터를 뷰에 설정
            binding.placeName.text = placeName
            binding.placeType.text = placeType
            binding.placeLocation.text = placeLocation?.split(" ")?.getOrNull(2) ?: ""
            binding.placeDistance.text = placeDistance
            binding.placeCall.text = placeCall
            binding.placeLocationFull.text = placeLocation

            // char1Text가 null이 아닌 경우에만 표시
            if (char1Text != null) {
                binding.char1.visibility = View.VISIBLE
                binding.char1Text.text = char1Text
            } else {
                binding.char1.visibility = View.GONE
            }

            // char2Text가 null이 아닌 경우에만 표시
            if (char2Text != null) {
                binding.char2.visibility = View.VISIBLE
                binding.char2Text.text = char2Text
            } else {
                binding.char2.visibility = View.GONE
            }

            // char3Text가 null이 아닌 경우에만 표시
            if (char3Text != null) {
                binding.char3.visibility = View.VISIBLE
                binding.char3Text.text = char3Text
            } else {
                binding.char3.visibility = View.GONE
            }
        }
    }

    // setupBackButton과 setupRecyclerView는 변경 없음
    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        // 기존 코드 유지
        imgDatas.clear()
        priceDatas.clear()

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

    private fun showSearchOptions() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_search_option, null)
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}