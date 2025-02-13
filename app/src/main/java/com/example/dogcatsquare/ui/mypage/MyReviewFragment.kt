package com.example.dogcatsquare.ui.mypage

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.data.api.MyPageRetrofitItf
import com.example.dogcatsquare.data.model.mypage.GetMyReviewResponse
import com.example.dogcatsquare.data.model.mypage.MyReview
import com.example.dogcatsquare.data.model.mypage.ReviewContent
import com.example.dogcatsquare.data.network.RetrofitObj
import com.example.dogcatsquare.databinding.FragmentMyReviewBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyReviewFragment : Fragment() {
    lateinit var binding: FragmentMyReviewBinding

    private var myReviewDatas = ArrayList<ReviewContent>()
    var currentPage = 1 // 현재 페이지 번호를 관리하는 변수

    private fun getToken(): String?{
        val sharedPref = activity?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPref?.getString("token", null)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyReviewBinding.inflate(inflater, container, false)

        setupMyReviewRecyclerView()

        return binding.root
    }

    private fun setupMyReviewRecyclerView() {
        myReviewDatas.clear()

        val myReviewRVAdapter = MyReviewRVAdapter(myReviewDatas)
        binding.myReviewRv.adapter = myReviewRVAdapter
        binding.myReviewRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        getMyReview(1, myReviewRVAdapter)
    }

    private fun getMyReview(page: Int, adapter: MyReviewRVAdapter) {
        val token = getToken()

        val getMyReviewService = RetrofitObj.getRetrofit().create(MyPageRetrofitItf::class.java)
        getMyReviewService.getMyReview("Bearer $token", page).enqueue(object : Callback<GetMyReviewResponse> {
            override fun onResponse(call: Call<GetMyReviewResponse>, response: Response<GetMyReviewResponse>) {
                Log.d("RETROFIT/SUCCESS", response.toString())
                val resp: GetMyReviewResponse = response.body()!!
                if (resp != null) {
                    if (resp.isSuccess) { // 응답 성공 시
                        Log.d("GetMyReview/SUCCESS", "레시피 목록 조회 성공")

                        // 서버에서 받은 댓글 데이터를 mycommentDatas에 추가
                        val reviews = resp.result.content.map { review ->
                            ReviewContent(
                                id = review.id,
                                content = review.content,
                                createdAt = review.createdAt,
                                imageUrls = review.imageUrls,
                            )
                        }

                        // 리스트에 새 데이터 추가
                        myReviewDatas.addAll(reviews)
                        adapter.notifyDataSetChanged() // 데이터 변경 알림

                    } else {
                        Log.e("GetMyReview/FAILURE", "응답 코드: ${resp.code}, 응답메시지: ${resp.message}")
                    }
                }
            }

            override fun onFailure(call: Call<GetMyReviewResponse>, t: Throwable) {
                Log.d("RETROFIT/FAILURE", t.message.toString())
            }
        })
    }
}