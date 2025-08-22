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
            subjects.any { it.contains("biography", true) ||
                    it.contains("memoir", true) } -> "Biography"

            subjects.any { it.contains("mystery", true) ||
                    it.contains("detective", true) ||
                    it.contains("crime", true) } -> "Mystery"

            subjects.any { it.contains("romance", true) ||
                    it.contains("love story", true) } -> "Romance"

            subjects.any { it.contains("fantasy", true) ||
                    it.contains("magic", true) ||
                    it.contains("wizard", true) } -> "Fantasy"

            subjects.any { it.contains("science fiction", true) ||
                    it.contains("sci-fi", true) ||
                    it.contains("space", true) ||
                    it.contains("future", true) } -> "Science Fiction"

            subjects.any { it.contains("history", true) ||
                    it.contains("historical", true) } -> "History"

            subjects.any { it.contains("science", true) &&
                    !it.contains("fiction", true) } -> "Science"

            subjects.any { it.contains("self-help", true) ||
                    it.contains("personal development", true) ||
                    it.contains("psychology", true) } -> "Self-Help"

            subjects.any { it.contains("fiction", true) ||
                    it.contains("novel", true) ||
                    it.contains("literature", true) } -> "Fiction"

            subjects.any { it.contains("non-fiction", true) ||
                    it.contains("nonfiction", true) } -> "Non-Fiction"

            else -> "Other"
        }
    }

    fun getSubjectsForDebug(): String {
        return subject?.joinToString(", ") ?: "No subjects"
    }
}