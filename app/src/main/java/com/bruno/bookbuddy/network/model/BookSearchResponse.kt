package com.bruno.bookbuddy.network.model

import com.google.gson.annotations.SerializedName

data class BookSearchResponse(
    @SerializedName("docs") val docs: List<BookApiItem>,
    @SerializedName("numFound") val numFound: Int,
    @SerializedName("start") val start: Int
)