package com.bruno.bookbuddy.utils

import com.bruno.bookbuddy.data.model.Genre

object GenreUtils {

    fun enumToDisplay(enumName: String): String {
        return when (enumName) {
            Genre.FICTION.name -> "Fiction"
            Genre.NON_FICTION.name -> "Non-Fiction"
            Genre.MYSTERY.name -> "Mystery"
            Genre.ROMANCE.name -> "Romance"
            Genre.SCIENCE_FICTION.name -> "Science Fiction"
            Genre.FANTASY.name -> "Fantasy"
            Genre.BIOGRAPHY.name -> "Biography"
            Genre.HISTORY.name -> "History"
            Genre.SELF_HELP.name -> "Self-Help"
            Genre.OTHER.name -> "Other"
            else -> "Other"
        }
    }

    fun displayToEnum(displayName: String): String {
        return when (displayName) {
            "Fiction" -> Genre.FICTION.name
            "Non-Fiction" -> Genre.NON_FICTION.name
            "Mystery" -> Genre.MYSTERY.name
            "Romance" -> Genre.ROMANCE.name
            "Science Fiction" -> Genre.SCIENCE_FICTION.name
            "Fantasy" -> Genre.FANTASY.name
            "Biography" -> Genre.BIOGRAPHY.name
            "History" -> Genre.HISTORY.name
            "Self-Help" -> Genre.SELF_HELP.name
            "Other" -> Genre.OTHER.name
            else -> Genre.OTHER.name
        }
    }

    fun getAllDisplayNames(): List<String> {
        return listOf(
            "Fiction",
            "Non-Fiction",
            "Mystery",
            "Romance",
            "Science Fiction",
            "Fantasy",
            "Biography",
            "History",
            "Self-Help",
            "Other"
        )
    }

    fun mapApiGenreToEnum(apiGenre: String): String {
        return when (apiGenre.lowercase()) {
            "fiction" -> Genre.FICTION.name
            "non-fiction", "nonfiction" -> Genre.NON_FICTION.name
            "mystery", "detective", "crime" -> Genre.MYSTERY.name
            "romance", "love story" -> Genre.ROMANCE.name
            "science fiction", "sci-fi", "science" -> Genre.SCIENCE_FICTION.name
            "fantasy", "magic" -> Genre.FANTASY.name
            "biography", "memoir" -> Genre.BIOGRAPHY.name
            "history", "historical" -> Genre.HISTORY.name
            "self-help", "psychology" -> Genre.SELF_HELP.name
            "other" -> Genre.OTHER.name
            else -> Genre.FICTION.name
        }
    }
}