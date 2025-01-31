package com.example.dogcatsquare.ui.wish

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.data.map.MapButton
import com.example.dogcatsquare.ui.map.location.MapButtonRVAdapter
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.wish.WishPlace
import com.example.dogcatsquare.databinding.FragmentWishPlaceBinding
import com.example.dogcatsquare.ui.map.location.SortDialogFragment

class WishPlaceFragment : Fragment() {
    private var _binding: FragmentWishPlaceBinding? = null
    private val binding get() = _binding!!

    private lateinit var sortTextView: TextView
    private var currentSortType = "주소기준"

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

        sortTextView = binding.sortButton.findViewById(R.id.sortText)

        setupRecyclerView()
        setupSortButton()
    }

    private fun setupRecyclerView() {
        buttonDatas.clear()
        placeDatas.clear()

        buttonDatas.apply {
            add(MapButton("전체"))
            add(MapButton("병원", R.drawable.btn_hospital))
            add(MapButton("산책로", R.drawable.btn_walk))
            add(MapButton("음식/카페", R.drawable.btn_restaurant))
            add(MapButton("호텔", R.drawable.btn_hotel))
        }

        val mapButtonRVAdapter = MapButtonRVAdapter(buttonDatas, object : MapButtonRVAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, buttonName: String) {
                when (buttonName) {
                    "전체" -> {
                        placeDatas.clear()
                        placeDatas.addAll(getAllPlaces())
                        binding.wishPlaceRV.adapter?.notifyDataSetChanged()
                    }
                    "병원" -> {
                        placeDatas.clear()
                        placeDatas.addAll(getAllPlaces().filter { it.placeType == "동물병원" })
                        binding.wishPlaceRV.adapter?.notifyDataSetChanged()
                    }
                    "산책로" -> {
                        placeDatas.clear()
                        placeDatas.addAll(getAllPlaces().filter { it.placeType == "산책로" })
                        binding.wishPlaceRV.adapter?.notifyDataSetChanged()
                    }
                    "음식/카페" -> {
                        placeDatas.clear()
                        placeDatas.addAll(getAllPlaces().filter { it.placeType == "식당" || it.placeType == "카페" })
                        binding.wishPlaceRV.adapter?.notifyDataSetChanged()
                    }
                    "호텔" -> {
                        placeDatas.clear()
                        placeDatas.addAll(getAllPlaces().filter { it.placeType == "호텔" })
                        binding.wishPlaceRV.adapter?.notifyDataSetChanged()
                    }
                }
            }
        })

        binding.mapButtonRV.apply {
            adapter = mapButtonRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        // 초기 데이터 설정 (전체)
        placeDatas.addAll(getAllPlaces())

        val wishPlaceRVAdapter = WishPlaceRVAdapter(placeDatas)
        binding.wishPlaceRV.apply {
            adapter = wishPlaceRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun getAllPlaces(): List<WishPlace> {
        return listOf(
            WishPlace(
                "ABC 고양이 호텔",
                "호텔",
                "0.55km",
                "서울시 양천구 중앙로 222 3층",
                "02-1234-5678",
                "고양이탁묘",    // char1Text
                "수영장",       // char2Text
                null,     // char3Text
                R.drawable.ic_place_img_default,
                "리뷰(18)"),
            WishPlace(
                "멍멍 동물병원",
                "동물병원",
                "0.75km",
                "서울시 강서구 화곡로 123",
                "02-2345-6789",
                "24시간 진료",  // char1Text
                "예방접종",     // char2Text
                null,  // char3Text
                R.drawable.ic_place_img_default,
                "리뷰(25)"),
            WishPlace(
                "행복한 산책로",
                "산책로",
                "1.2km",
                "서울시 마포구 망원동 45-1",
                null,
                "난이도 하",    // char1Text
                "쓰레기통",     // char2Text
                null,          // char3Text
                R.drawable.ic_place_img_default,
                "리뷰(32)"),
            WishPlace(
                "펫프렌들리 카페",
                "카페",
                "0.3km",
                "서울시 서대문구 연희동 89",
                "02-3456-7890",
                "야외 테라스",  // char1Text
                "반려동물 간식", // char2Text
                null,     // char3Text
                R.drawable.ic_place_img_default,
                "리뷰(15)"),
            WishPlace(
                "럭셔리 펫호텔",
                "호텔",
                "1.5km",
                "서울시 강남구 역삼동 123",
                "02-4567-8901",
                "수영장",      // char1Text
                "호텔식 사료",  // char2Text
                null,  // char3Text
                R.drawable.ic_place_img_default,
                "리뷰(42)")
        )
    }

    private fun setupSortButton() {
        binding.sortButton.setOnClickListener {
            val sortDialog = SortDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("currentSortType", currentSortType)
                }
            }
            sortDialog.show(childFragmentManager, "SortDialog")
        }
    }

    fun updateSortText(sortType: String) {
        currentSortType = sortType
        activity?.runOnUiThread {
            try {
                sortTextView.text = sortType
            } catch (e: Exception) {
                Log.e("WishPlaceFragment", "Error updating sort text: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}