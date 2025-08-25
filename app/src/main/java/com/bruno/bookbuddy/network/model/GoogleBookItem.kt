package com.bruno.bookbuddy.network.model

import com.google.gson.annotations.SerializedName

data class GoogleBookSearchResponse(
    @SerializedName("items") val items: List<GoogleBookItem>?,
    @SerializedName("totalItems") val totalItems: Int,
    @SerializedName("kind") val kind: String
)

data class GoogleBookItem(
    @SerializedName("id") val id: String?,
    @SerializedName("volumeInfo") val volumeInfo: VolumeInfo,
    @SerializedName("searchInfo") val searchInfo: SearchInfo?
)

data class VolumeInfo(
    @SerializedName("title") val title: String?,
    @SerializedName("authors") val authors: List<String>?,
    @SerializedName("publishedDate") val publishedDate: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("categories") val categories: List<String>?,
    @SerializedName("imageLinks") val imageLinks: ImageLinks?,
    @SerializedName("language") val language: String?,
    @SerializedName("pageCount") val pageCount: Int?,
    @SerializedName("publisher") val publisher: String?,
    @SerializedName("averageRating") val averageRating: Float?
)

data class ImageLinks(
    @SerializedName("smallThumbnail") val smallThumbnail: String?,
    @SerializedName("thumbnail") val thumbnail: String?,
    @SerializedName("small") val small: String?,
    @SerializedName("medium") val medium: String?,
    @SerializedName("large") val large: String?
)

data class SearchInfo(
    @SerializedName("textSnippet") val textSnippet: String?
)

fun GoogleBookItem.getMainAuthor(): String {
    return volumeInfo.authors?.firstOrNull() ?: "Unknown Author"
}

fun GoogleBookItem.getPublishYear(): Int {
    val dateString = volumeInfo.publishedDate ?: return 0

    return try {
        when {
            dateString.length >= 4 -> dateString.substring(0, 4).toInt()
            else -> 0
        }
    } catch (e: NumberFormatException) {
        0
    }
}

fun GoogleBookItem.getCoverUrl(): String? {
    return volumeInfo.imageLinks?.let { images ->
        images.large
            ?: images.medium
            ?: images.thumbnail
            ?: images.small
            ?: images.smallThumbnail
    }
}

fun GoogleBookItem.getMainGenre(): String {
    val categories = volumeInfo.categories

    if (categories.isNullOrEmpty()) {
        return "Fiction"
    }

    val firstCategory = categories.first().lowercase()

    return when {
        firstCategory.contains("biography") || firstCategory.contains("memoir") -> "Biography"
        firstCategory.contains("science fiction") || firstCategory.contains("sci-fi") -> "Science Fiction"
        firstCategory.contains("mystery") || firstCategory.contains("detective") || firstCategory.contains("crime") -> "Mystery"
        firstCategory.contains("romance") -> "Romance"
        firstCategory.contains("fantasy") -> "Fantasy"
        firstCategory.contains("history") || firstCategory.contains("historical") -> "History"
        firstCategory.contains("self-help") || firstCategory.contains("psychology") -> "Self-Help"
        firstCategory.contains("fiction") -> "Fiction"
        firstCategory.contains("non-fiction") || firstCategory.contains("nonfiction") -> "Non-Fiction"
        else -> "Other"
    }
}

fun GoogleBookItem.getDescription(): String {
    return volumeInfo.description
        ?: searchInfo?.textSnippet
        ?: "No description available"
}