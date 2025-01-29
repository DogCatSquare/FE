package com.example.dogcatsquare.data.community

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface BoardApiService {

    // 게시판 전체 목록 가져오기 (홈 화면에서 사용)
    @GET("/api/board/posts")
    fun getAllPosts(): Call<List<BoardResponse>>

    // 특정 게시판 가져오기 (게시판 상세 보기에서 사용)
    @GET("/api/board/{boardId}")
    fun getBoard(@Path("boardId") boardId: Long): Call<BoardResponse>
}
