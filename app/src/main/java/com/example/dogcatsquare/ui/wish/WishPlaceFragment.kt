package com.example.dogcatsquare.ui.wish

import android.content.Context
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
import com.example.dogcatsquare.data.api.WishRetrofitObj
import com.example.dogcatsquare.data.map.MyLocation
import com.example.dogcatsquare.data.model.wish.GetMyWishResponse
import com.example.dogcatsquare.data.model.wish.WishPlace
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.FragmentWishPlaceBinding
import com.example.dogcatsquare.ui.map.location.MapDetailFragment
import com.example.dogcatsquare.ui.map.location.SortDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WishPlaceFragment : Fragment() {
    private var _binding: FragmentWishPlaceBinding? = null
    private val binding get() = _binding!!

    private lateinit var sortTextView: TextView
    private var currentSortType = "주소기준"

    private val buttonDatas by lazy { ArrayList<MapButton>() }
    private val placeDatas by lazy { ArrayList<WishPlace>() }

    private val categoryMap = mapOf(
        "HOSPITAL" to "동물병원",
        "CAFE" to "카페",
        "RESTAURANT" to "식당",
        "HOTEL" to "호텔",
        "ETC" to "기타"
    )

    private fun getToken(): String? {
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

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
            add(MapButton("음식/카페", R.drawable.btn_restaurant))
            add(MapButton("호텔", R.drawable.btn_hotel))
            add(MapButton("기타", R.drawable.btn_etc))
        }

        val mapButtonRVAdapter = MapButtonRVAdapter(buttonDatas, object : MapButtonRVAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, buttonName: String) {
                getAllPlaces { places ->
                    placeDatas.clear()
                    placeDatas.addAll(
                        when (buttonName) {
                            "전체" -> places
                            "병원" -> places.filter { it.category == "동물병원" }
                            "음식/카페" -> places.filter { it.category == "식당" || it.category == "카페" }
                            "호텔" -> places.filter { it.category == "호텔" }
                            "기타" -> places.filter { it.category == "기타" }
                            else -> emptyList()
                        }
                    )
                    binding.wishPlaceRV.adapter?.notifyDataSetChanged()
                }
            }
        })

        binding.mapButtonRV.apply {
            adapter = mapButtonRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        val wishPlaceRVAdapter = WishPlaceRVAdapter(placeDatas, getToken())
        binding.wishPlaceRV.apply {
            adapter = wishPlaceRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        // 🔥 초기 데이터 로드
        getAllPlaces { places ->
            placeDatas.addAll(places)
            binding.wishPlaceRV.adapter?.notifyDataSetChanged()
        }

        wishPlaceRVAdapter.setMyItemClickListener(object : WishPlaceRVAdapter.OnItemClickListener{
            override fun onItemClick(place: WishPlace) {
                // placeType에 따라 다른 Fragment로 전환
                if (place.category == "병원") {
                    val fragment = MapDetailFragment.newInstance(place.id, 0.0, 0.0)
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, fragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
                } else {
                    val fragment = MapDetailFragment.newInstance(place.id, 0.0, 0.0)
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frm, fragment)
                        .addToBackStack(null)
                        .commitAllowingStateLoss()
                }
            }

        })
    }

    private fun getAllPlaces(callback: (List<WishPlace>) -> Unit) {
        val token = getToken()

        val getMyWishService = RetrofitObj.getRetrofit().create(WishRetrofitObj::class.java)
        // 위도 경도 기본값 -> 추후 수정
        getMyWishService.getMyWish("Bearer $token", MyLocation(37.5665, 126.9780)).enqueue(object : Callback<GetMyWishResponse> {
            override fun onResponse(call: Call<GetMyWishResponse>, response: Response<GetMyWishResponse>) {
                Log.d("GetMyWish/SUCCESS", response.toString())
                val resp: GetMyWishResponse = response.body()!!

                if (resp != null) {
                    if (resp.isSuccess) {
                        Log.d("GetMyWish", "내 위시 전체 조회 성공")

                        val wishes = resp.result.map { wish ->
                            WishPlace(
                                id = wish.id,
                                name = wish.name,
                                address = wish.address,
                                category = categoryMap[wish.category] ?: wish.category,
                                phoneNumber = wish.phoneNumber ?: "",
                                longitude = wish.longitude,
                                latitude = wish.latitude,
                                distance = wish.distance,
                                open = wish.open ?: false,
                                imgUrl = wish.imgUrl ?: "",
                                reviewCount = wish.reviewCount,
                                keywords = wish.keywords,
                                isWish = true
                            )
                        }.filter { it.category != "PARK" } // "산책로"인 경우 제외

                        callback(wishes)
                    }
                } else {
                    callback(emptyList())
                }
            }

            override fun onFailure(call: Call<GetMyWishResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }

        })
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