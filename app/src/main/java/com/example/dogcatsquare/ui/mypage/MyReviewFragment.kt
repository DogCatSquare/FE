package com.example.dogcatsquare.ui.mypage

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dogcatsquare.data.api.MyPageRetrofitItf
import com.example.dogcatsquare.data.api.ReviewRetrofitItf
import com.example.dogcatsquare.data.model.map.DeleteReviewResponse
import com.example.dogcatsquare.data.model.mypage.GetMyReviewResponse
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
    private val id: Int = -1
    private val walkId: Int? = null
    private val placeId: Int? = null

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

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return binding.root
    }

    private fun setupMyReviewRecyclerView() {
        myReviewDatas.clear()

        val myReviewRVAdapter = MyReviewRVAdapter(myReviewDatas) { params ->
            onDeleteReview(params.reviewId, params.placeId, params.walkId)
        }
        binding.myReviewRv.adapter = myReviewRVAdapter
        binding.myReviewRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        getMyReview(myReviewRVAdapter)
    }

    private fun getMyReview(adapter: MyReviewRVAdapter) {
        val token = getToken()

        val getMyReviewService = RetrofitObj.getRetrofit(requireContext()).create(MyPageRetrofitItf::class.java)
        getMyReviewService.getMyReview("Bearer $token", 0).enqueue(object : Callback<GetMyReviewResponse> {
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
                                title = review.title,
                                content = review.content,
                                createdAt = com.example.dogcatsquare.util.DateFmt.format(review.createdAt).replace(".", "-"),
                                imageUrls = review.imageUrls,
                                placeId = review.placeId,
                                walkId = review.walkId
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

    private fun onDeleteReview(reviewId: Int, placeId: Int?, walkId: Int?) {
        val token = getToken()
        val deleteReviewService = RetrofitObj.getRetrofit(requireContext()).create(ReviewRetrofitItf::class.java)

        if (walkId == null) {
            if (placeId != null) {
                deleteReviewService.deletePlaceReview("Bearer $token", placeId, reviewId).enqueue(object : Callback<DeleteReviewResponse> {
                    override fun onResponse(call: Call<DeleteReviewResponse>, response: Response<DeleteReviewResponse>) {
                        Log.d("RETROFIT/SUCCESS", response.toString())

                        if (response.isSuccessful) {
                            response.body()?.let { resp ->
                                if (resp.isSuccess) {
                                    Log.d("DeletePlaceReview/SUCCESS", "DeletePlaceReview")

                                    Toast.makeText(context, "리뷰 삭제가 완료되었습니다", Toast.LENGTH_SHORT).show()
                                } else {
                                    Log.e(
                                        "DeletePlaceReview/FAILURE",
                                        "응답 코드: ${resp.code}, 응답 메시지: ${resp.message}"
                                    )
                                    Toast.makeText(context, "오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<DeleteReviewResponse>, t: Throwable) {
                        Log.d("RETROFIT/FAILURE", t.message.toString())                }

                })
            }
        } else {
            deleteReviewService.deleteWalkReview("Bearer $token", walkId, reviewId).enqueue(object :
                Callback<DeleteReviewResponse> {
                override fun onResponse(call: Call<DeleteReviewResponse>, response: Response<DeleteReviewResponse>) {
                    Log.d("RETROFIT/SUCCESS", response.toString())

                    if (response.isSuccessful) {
                        response.body()?.let { resp ->
                            if (resp.isSuccess) {
                                Log.d("DeleteWalkReview/SUCCESS", "DeleteWalkReview")

                                Toast.makeText(context, "리뷰 삭제가 완료되었습니다", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.e(
                                    "DeleteWalkReview/FAILURE",
                                    "응답 코드: ${resp.code}, 응답 메시지: ${resp.message}"
                                )
                                Toast.makeText(context, "오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<DeleteReviewResponse>, t: Throwable) {
                    Log.d("RETROFIT/FAILURE", t.message.toString())                }

            })
        }
    }
}