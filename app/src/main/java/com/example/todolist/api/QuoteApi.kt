package com.example.todolist.api

import retrofit2.http.GET

data class QuoteResponse(
    val content: String,
    val author: String
)

interface QuoteApi {
    @GET("random")
    suspend fun getRandomQuote(): QuoteResponse
}