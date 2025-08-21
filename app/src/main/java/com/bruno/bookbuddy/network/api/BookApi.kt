package com.bruno.bookbuddy.network.api

import com.bruno.bookbuddy.network.model.BookSearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

const val OPEN_LIBRARY_BASE_URL = "https://openlibrary.org/"

interface BookApi {

    @GET("search.json")
    fun searchBooks(
        @Query("title") title: String? = null,
        @Query("author") author: String? = null,
        @Query("limit") limit: Int = 10
    ): Call<BookSearchResponse>

    @GET("search.json")
    fun searchBooksWithOffset(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): Call<BookSearchResponse>
}