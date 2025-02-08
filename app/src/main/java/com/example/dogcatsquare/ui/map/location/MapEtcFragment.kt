package com.example.dogcatsquare.ui.map.location

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.data.map.DetailImg
import com.example.dogcatsquare.data.map.MapPrice
import com.example.dogcatsquare.data.map.MapReview
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentMapEtcBinding
import com.example.dogcatsquare.ui.map.SearchFragment
import com.google.android.material.bottomsheet.BottomSheetDialog

class MapEtcFragment : Fragment() {
    private var _binding: FragmentMapEtcBinding? = null
    private val binding get() = _binding!!

    private val imgDatas by lazy { ArrayList<DetailImg>() }
    private val priceDatas by lazy { ArrayList<MapPrice>() }
    private val reviewDatas by lazy { ArrayList<MapReview>() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapEtcBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton()
        setupRecyclerView()
        setupAddReviewButton()

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

            // 로그 추가
            Log.d("MapEtcFragment", "char1Text: $char1Text")
            Log.d("MapEtcFragment", "char2Text: $char2Text")
            Log.d("MapEtcFragment", "char3Text: $char3Text")

            // 받아온 데이터를 뷰에 설정
            binding.placeName.text = placeName
            binding.placeType.text = placeType
            binding.placeLocation.text = placeLocation?.split(" ")?.getOrNull(2) ?: ""
            binding.placeDistance.text = placeDistance
            binding.placeCall.text = placeCall
            binding.placeLocationFull.text = placeLocation

            // char1Text가 null이 아닌 경우에만 표시
            if (!char1Text.isNullOrEmpty()) {
                binding.char1.visibility = View.VISIBLE
                binding.char1Text.text = char1Text
            } else {
                binding.char1.visibility = View.GONE
            }

            // char2Text가 null이 아닌 경우에만 표시
            if (!char2Text.isNullOrEmpty()) {
                binding.char2.visibility = View.VISIBLE
                binding.char2Text.text = char2Text
            } else {
                binding.char2.visibility = View.GONE
            }

            // char3Text가 null이 아닌 경우에만 표시
            if (!char3Text.isNullOrEmpty()) {
                binding.char3.visibility = View.VISIBLE
                binding.char3Text.text = char3Text
            } else {
                binding.char3.visibility = View.GONE
            }

            // placeType에 따른 특성 카드의 스타일 설정
            when (placeType) {
                "호텔" -> {
                    binding.char1.setCardBackgroundColor(Color.parseColor("#FEEEEA"))
                    binding.char1Text.setTextColor(Color.parseColor("#F36037"))
                    binding.char2.setCardBackgroundColor(Color.parseColor("#FEEEEA"))
                    binding.char2Text.setTextColor(Color.parseColor("#F36037"))
                    binding.char3.setCardBackgroundColor(Color.parseColor("#FEEEEA"))
                    binding.char3Text.setTextColor(Color.parseColor("#F36037"))
                    binding.reserveButton.visibility = View.VISIBLE
                }
                "식당" -> {
                    binding.char1.setCardBackgroundColor(Color.parseColor("#FFFBF1"))
                    binding.char1Text.setTextColor(Color.parseColor("#FF8D41"))
                    binding.char2.setCardBackgroundColor(Color.parseColor("#FFFBF1"))
                    binding.char2Text.setTextColor(Color.parseColor("#FF8D41"))
                    binding.char3.setCardBackgroundColor(Color.parseColor("#FFFBF1"))
                    binding.char3Text.setTextColor(Color.parseColor("#FF8D41"))
                    binding.reserveButton.visibility = View.GONE
                }
                "카페" -> {
                    binding.char1.setCardBackgroundColor(Color.parseColor("#FFFBF1"))
                    binding.char1Text.setTextColor(Color.parseColor("#FF8D41"))
                    binding.char2.setCardBackgroundColor(Color.parseColor("#FFFBF1"))
                    binding.char2Text.setTextColor(Color.parseColor("#FF8D41"))
                    binding.char3.setCardBackgroundColor(Color.parseColor("#FFFBF1"))
                    binding.char3Text.setTextColor(Color.parseColor("#FF8D41"))
                    binding.reserveButton.visibility = View.GONE
                }
            }
        }
    }

    // 나머지 함수들은 그대로 유지
    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        // 기존 코드 유지
        imgDatas.clear()
        priceDatas.clear()
        reviewDatas.clear()

        // 이미지 데이터 설정
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

        // 가격 데이터 설정
        priceDatas.apply {
            add(MapPrice("스텐다드", "30,000원"))
            add(MapPrice("디럭스", "30,000원"))
            add(MapPrice("VIP룸", "30,000원"))
            add(MapPrice("1묘 추가비", "30,000원"))
        }

        val mapPriceRVAdapter = MapPriceRVAdapter(priceDatas)
        binding.mapPriceRV.apply {
            adapter = mapPriceRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        // 리뷰 데이터 설정
        reviewDatas.apply {
            add(MapReview(R.drawable.ic_profile_img_default, "닉네임", "포메라니안", "강아지 종합접종이랑 중성화했어요 의사선생님이 친절하시고 꼼꼼히 봐주셔서 좋아요. 다음에 건강검진도 이곳에... 더보기", "2024.01.04", R.drawable.ic_place_img_default))
            add(MapReview(R.drawable.ic_profile_img_default, "닉네임", "포메라니안", "두 번째 리뷰 내용...", "2024.01.04", R.drawable.ic_place_img_default))
            add(MapReview(R.drawable.ic_profile_img_default, "닉네임", "골든리트리버", "세 번째 리뷰 내용...", "2024.01.03", R.drawable.ic_place_img_default))
            add(MapReview(R.drawable.ic_profile_img_default, "닉네임", "시바견", "네 번째 리뷰 내용...", "2024.01.02", R.drawable.ic_place_img_default))
        }

        // reviewCount 텍스트 설정
        binding.reviewCount.text = reviewDatas.size.toString()

        // 처음 2개의 리뷰만 표시
        val displayedReviews = ArrayList<MapReview>().apply {
            addAll(reviewDatas.take(2))
        }

        val mapReviewRVAdapter = MapReviewRVAdapter(displayedReviews)
        binding.reviewRV.apply {
            adapter = mapReviewRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        binding.reviewPlus.setOnClickListener {
            val mapReviewFragment = MapReviewFragment()

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, mapReviewFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupAddReviewButton() {
        binding.addButton.setOnClickListener {
            val mapAddReviewFragment = MapAddReviewFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, mapAddReviewFragment)
                .addToBackStack(null)
                .commit()
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
        // 이전 Fragment를 다시 보이게 함
        requireActivity().supportFragmentManager.fragments
            .filterIsInstance<MapFragment>()
            .firstOrNull()?.let { mapFragment ->
                requireActivity().supportFragmentManager.beginTransaction()
                    .show(mapFragment)
                    .commit()
            }
        _binding = null
    }
}