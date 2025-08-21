package com.bruno.bookbuddy.network.model

import com.google.gson.annotations.SerializedName

data class BookApiItem(
    @SerializedName("title") val title: String?,
    @SerializedName("author_name") val authorName: List<String>?,
    @SerializedName("first_publish_year") val firstPublishYear: Int?,
    @SerializedName("isbn") val isbn: List<String>?,
    @SerializedName("cover_i") val coverI: Int?,
    @SerializedName("subject") val subject: List<String>?,
    @SerializedName("key") val key: String?,
    @SerializedName("publisher") val publisher: List<String>?,
    @SerializedName("language") val language: List<String>?,
    @SerializedName("page_count_median") val pageCountMedian: Int?
) {

    fun getMainAuthor(): String {
        return authorName?.firstOrNull() ?: "Unknown Author"
    }

    fun getPublishYear(): Int {
        return firstPublishYear ?: 0
    }

    fun getCoverUrl(): String? {
        return coverI?.let {
            "https://covers.openlibrary.org/b/id/$it-M.jpg"
        }
    }

    fun getMainGenre(): String {
        val subjects = subject ?: return "Fiction"

        return when {
            subjects.any { it.contains("fiction", true) } -> "Fiction"
            subjects.any { it.contains("science", true) } -> "Science"
            subjects.any { it.contains("history", true) } -> "History"
            subjects.any { it.contains("biography", true) } -> "Biography"
            subjects.any { it.contains("romance", true) } -> "Romance"
            subjects.any { it.contains("mystery", true) } -> "Mystery"
            subjects.any { it.contains("fantasy", true) } -> "Fantasy"
            else -> "Other"
        }
    }
}