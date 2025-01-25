package com.example.dogcatsquare.ui.map.location

import android.os.Bundle
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
        imgDatas.clear()
        priceDatas.clear()
        reviewDatas.clear()

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

        reviewDatas.apply {
            add(MapReview(R.drawable.ic_profile_img_default, "닉네임", "포메라니안", "강아지 종합접종이랑 중성화했어요 의사선생님이 친절하시고 꼼꼼히 봐주셔서 좋아요. 다음에 건강검진도 이곳에... 더보기", "2024.01.04", R.drawable.ic_place_img_default))
            add(MapReview(R.drawable.ic_profile_img_default, "닉네임", "포메라니안", "두 번째 리뷰 내용...", "2024.01.04", R.drawable.ic_place_img_default))
            add(MapReview(R.drawable.ic_profile_img_default, "닉네임", "골든리트리버", "세 번째 리뷰 내용...", "2024.01.03", R.drawable.ic_place_img_default))
            add(MapReview(R.drawable.ic_profile_img_default, "닉네임", "시바견", "네 번째 리뷰 내용...", "2024.01.02", R.drawable.ic_place_img_default))
        }

        // ArrayList로 변환하여 처음 2개만 표시
        val displayedReviews = ArrayList<MapReview>().apply {
            addAll(reviewDatas.take(2))
        }

        val mapReviewRVAdapter = MapReviewRVAdapter(displayedReviews)
        binding.reviewRV.apply {
            adapter = mapReviewRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        // reviewPlus 버튼 표시 여부 설정
        binding.reviewPlus.visibility = if (reviewDatas.size > 2) View.VISIBLE else View.GONE

        // reviewPlus 클릭 리스너 - 단순 화면 전환
        binding.reviewPlus.setOnClickListener {
            val mapReviewFragment = MapReviewFragment()

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, mapReviewFragment)
                .addToBackStack(null)
                .commit()
        }


    }

    private fun setupAddReviewButton() {
        binding.addReview.setOnClickListener {
            val mapAddReviewFragment = MapAddReviewFragment()

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.main_frm, mapAddReviewFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}