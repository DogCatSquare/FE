package com.example.dogcatsquare.data.api

import com.example.dogcatsquare.data.model.quiz.GetRandomQuizResponse
import com.example.dogcatsquare.data.model.quiz.SubmitQuizAnswerRequest
import com.example.dogcatsquare.data.model.quiz.SubmitQuizAnswerResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface QuizRetrofitItf {
    @GET("/api/quiz/random")
    fun getRandomQuiz(): Call<GetRandomQuizResponse>

    @POST("/api/quiz/{quizId}/answer")
    fun submitQuizAnswer(
        @Path("quizId") quizId: Long,
        @Body request: SubmitQuizAnswerRequest
    ): Call<SubmitQuizAnswerResponse>
}
