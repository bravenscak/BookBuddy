package com.bruno.bookbuddy.network.api

import com.bruno.bookbuddy.network.model.GoogleBookSearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

const val GOOGLE_BOOKS_BASE_URL = "https://www.googleapis.com/books/v1/"

interface BookApi {

    @GET("volumes")
    fun searchBooks(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 10
    ): Call<GoogleBookSearchResponse>

    @GET("volumes")
    fun searchBooksByTitleAndAuthor(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 10
    ): Call<GoogleBookSearchResponse>
}