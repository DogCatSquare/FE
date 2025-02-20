package com.example.dogcatsquare.ui.wish

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.R
import com.example.dogcatsquare.data.api.WishRetrofitObj
import com.example.dogcatsquare.data.map.MapButton
import com.example.dogcatsquare.data.map.MyLocation
import com.example.dogcatsquare.data.model.wish.GetMyWishResponse
import com.example.dogcatsquare.data.model.wish.WishPlace
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.FragmentWishPlaceBinding
import com.example.dogcatsquare.databinding.FragmentWishWalkBinding
import com.example.dogcatsquare.ui.map.location.MapButtonRVAdapter
import com.example.dogcatsquare.ui.map.location.MapDetailFragment
import com.example.dogcatsquare.ui.map.location.SortDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WishWalkFragment : Fragment() {
    private var _binding: FragmentWishWalkBinding? = null
    private val binding get() = _binding!!
    private lateinit var wishWalkAdapter: WishWalkAdapter

    private lateinit var sortTextView: TextView
    private var currentSortType = "주소기준"

    private val placeDatas by lazy { ArrayList<WishPlace>() }

    private val categoryMap = mapOf(
        "PARK" to "산책로"
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
        _binding = FragmentWishWalkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        placeDatas.clear()

        wishWalkAdapter = WishWalkAdapter(placeDatas, getToken())
        binding.walkRV.apply {
            adapter = wishWalkAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        // 🔥 초기 데이터 로드
        getAllPlaces { places ->
            placeDatas.addAll(places)
            binding.walkRV.adapter?.notifyDataSetChanged()
        }

        wishWalkAdapter.setMyItemClickListener(object : WishWalkAdapter.OnItemClickListener{
            override fun onItemClick(place: WishPlace) {
                // 상세 조회 부분
            }

        })
    }

    private fun getAllPlaces(callback: (List<WishPlace>) -> Unit) {
        val token = getToken()

        val getMyWishService = RetrofitObj.getRetrofit().create(WishRetrofitObj::class.java)
        // 위도 경도 기본값 -> 추후 수정
        getMyWishService.getMyWish("Bearer $token", MyLocation(37.5665, 126.9780)).enqueue(object :
            Callback<GetMyWishResponse> {
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
                        }.filter { it.category == "산책로" } // "산책로"인 경우만

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