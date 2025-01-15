package com.example.dogcatsquare.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.MapButton
import com.example.dogcatsquare.MapButtonRVAdapter
import com.example.dogcatsquare.MapPlace
import com.example.dogcatsquare.MapPlaceRVAdapter
import com.example.dogcatsquare.R
import com.example.dogcatsquare.databinding.FragmentMapFullBinding

class MapFullFragment : Fragment() {
    private var _binding: FragmentMapFullBinding? = null
    private var buttonDatas = ArrayList<MapButton>()
    private var placeDatas = ArrayList<MapPlace>()
    private val binding get() = _binding!!

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
            add(MapPlace("가나다 동물병원", "동물병원", "0.55km", "서울시 성북구 월곡동 77", "02-1234-5678", "중성화 수술", R.drawable.ic_place_img_default))
            add(MapPlace("서대문 안산자락길", "산책로", "0.55km", "서울시 서대문구 봉원사길 75-66", "02-1234-5678", "쓰레기통", R.drawable.ic_place_img_default))
            add(MapPlace("다나가 동물병원", "동물병원", "0.55km", "서울시 성북구 월곡동 77", "02-1234-5678", "중성화 수술", R.drawable.ic_place_img_default))
            add(MapPlace("가나다 동물병원", "동물병원", "0.55km", "서울시 성북구 월곡동 77", "02-1234-5678", "중성화 수술", R.drawable.ic_place_img_default))
        }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}