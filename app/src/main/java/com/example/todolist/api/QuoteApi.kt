package com.example.todolist.api

import retrofit2.http.GET

data class QuoteResponse(
    val q: String,
    val a: String
)

interface QuoteApi {
    @GET("random")
    suspend fun getRandomQuote(): List<QuoteResponse>
}