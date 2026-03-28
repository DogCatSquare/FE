package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.model.announcement.Notice
import com.example.dogcatsquare.data.model.map.BaseResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface NoticeRetrofitItf {
    @GET("/api/notices")
    fun getNotices(): Call<BaseResponse<List<Notice>>>

    @GET("/api/notices/{id}")
    fun getNoticeDetail(
        @Path("id") id: Long
    ): Call<BaseResponse<Notice>>
}
