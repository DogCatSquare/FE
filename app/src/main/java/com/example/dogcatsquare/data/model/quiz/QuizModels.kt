package com.example.dogcatsquare.data.model.quiz

import com.google.gson.annotations.SerializedName

data class GetRandomQuizResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: QuizResult
)

data class QuizResult(
    val quizId: Long,
    val question: String
)

data class SubmitQuizAnswerRequest(
    val selectedAnswer: String
)

data class SubmitQuizAnswerResponse(
    val isSuccess: Boolean,
    val code: String,
    val message: String,
    val result: SubmitQuizResult
)

data class SubmitQuizResult(
    @SerializedName("isCorrect", alternate = ["is_correct"]) val isCorrect: Boolean,
    @SerializedName("correctAnswer", alternate = ["correct_answer"]) val correctAnswer: String?,

    val explanation: String
)
