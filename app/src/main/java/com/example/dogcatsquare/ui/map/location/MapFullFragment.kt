package com.example.dogcatsquare.ui.map.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.data.map.MapButton
import com.example.dogcatsquare.data.map.MapPlace
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentMapFullBinding

class MapFullFragment : Fragment() {
    private var _binding: FragmentMapFullBinding? = null
    private val binding get() = _binding!!

    private val buttonDatas by lazy { ArrayList<MapButton>() }
    private val placeDatas by lazy { ArrayList<MapPlace>() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapFullBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton()
        setupRecyclerView()
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
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
                        binding.mapPlaceRV.adapter?.notifyDataSetChanged()
                    }
                    "병원" -> {
                        placeDatas.clear()
                        placeDatas.addAll(getAllPlaces().filter { it.placeType == "동물병원" })
                        binding.mapPlaceRV.adapter?.notifyDataSetChanged()
                    }
                    "산책로" -> {
                        placeDatas.clear()
                        placeDatas.addAll(getAllPlaces().filter { it.placeType == "산책로" })
                        binding.mapPlaceRV.adapter?.notifyDataSetChanged()
                    }
                    "음식/카페" -> {
                        placeDatas.clear()
                        placeDatas.addAll(getAllPlaces().filter { it.placeType == "음식/카페" })
                        binding.mapPlaceRV.adapter?.notifyDataSetChanged()
                    }
                    "호텔" -> {
                        placeDatas.clear()
                        placeDatas.addAll(getAllPlaces().filter { it.placeType == "호텔" })
                        binding.mapPlaceRV.adapter?.notifyDataSetChanged()
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

        val mapPlaceRVAdapter = MapPlaceRVAdapter(placeDatas, object : MapPlaceRVAdapter.OnItemClickListener {
            override fun onItemClick(place: MapPlace) {
                // placeType에 따라 다른 Fragment로 전환
                val fragment = if (place.placeType == "동물병원") {
                    MapDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString("placeName", place.placeName)
                            putString("placeType", place.placeType)
                            putString("placeDistance", place.placeDistance)
                            putString("placeLocation", place.placeLocation)
                            putString("placeCall", place.placeCall)
                            putString("placeChar1", place.placeChar1)
                            place.placeImg?.let { putInt("placeImg", it) }
                        }
                    }
                } else {
                    MapEtcFragment().apply {
                        arguments = Bundle().apply {
                            putString("placeName", place.placeName)
                            putString("placeType", place.placeType)
                            putString("placeDistance", place.placeDistance)
                            putString("placeLocation", place.placeLocation)
                            putString("placeCall", place.placeCall)
                            putString("placeChar1", place.placeChar1)
                            place.placeImg?.let { putInt("placeImg", it) }
                        }
                    }
                }

                // Fragment 전환
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.main_frm, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        })

        binding.mapPlaceRV.apply {
            adapter = mapPlaceRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    private fun getAllPlaces(): List<MapPlace> {
        return listOf(
            MapPlace("가나다 동물병원", "동물병원", "0.55km", "서울시 성북구 월곡동 77", "02-1234-5678", "중성화 수술", R.drawable.ic_place_img_default),
            MapPlace("서대문 안산자락길", "산책로", "0.55km", "서울시 서대문구 봉원사길 75-66", "02-1234-5678", "쓰레기통", R.drawable.ic_place_img_default),
            MapPlace("다나가 동물병원", "동물병원", "0.55km", "서울시 성북구 월곡동 77", "02-1234-5678", "중성화 수술", R.drawable.ic_place_img_default),
            MapPlace("가나다 동물병원", "동물병원", "0.55km", "서울시 성북구 월곡동 77", "02-1234-5678", "중성화 수술", R.drawable.ic_place_img_default),
            MapPlace("서대문 안산자락길", "산책로", "0.55km", "서울시 서대문구 봉원사길 75-66", "02-1234-5678", "쓰레기통", R.drawable.ic_place_img_default),
            MapPlace("가나다 동물병원", "동물병원", "0.55km", "서울시 성북구 월곡동 77", "02-1234-5678", "중성화 수술", R.drawable.ic_place_img_default),
            MapPlace("가나다 동물병원", "동물병원", "0.55km", "서울시 성북구 월곡동 77", "02-1234-5678", "중성화 수술", R.drawable.ic_place_img_default)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}