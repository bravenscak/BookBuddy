package com.bruno.bookbuddy.utils

import android.content.Context
import com.bruno.bookbuddy.R
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
            "Fiction", "Fikcija" -> Genre.FICTION.name
            "Non-Fiction", "Nefikcija" -> Genre.NON_FICTION.name
            "Mystery", "Misterij" -> Genre.MYSTERY.name
            "Romance", "Romansa" -> Genre.ROMANCE.name
            "Science Fiction", "Znanstvena fantastika" -> Genre.SCIENCE_FICTION.name
            "Fantasy", "Fantazija" -> Genre.FANTASY.name
            "Biography", "Biografija" -> Genre.BIOGRAPHY.name
            "History", "Povijest" -> Genre.HISTORY.name
            "Self-Help", "Samopomoć" -> Genre.SELF_HELP.name
            "Other", "Ostalo" -> Genre.OTHER.name
            else -> Genre.OTHER.name
        }
    }

    fun getAllDisplayNames(context: Context): List<String> {
        return listOf(
            context.getString(R.string.genre_fiction),
            context.getString(R.string.genre_non_fiction),
            context.getString(R.string.genre_mystery),
            context.getString(R.string.genre_romance),
            context.getString(R.string.genre_science_fiction),
            context.getString(R.string.genre_fantasy),
            context.getString(R.string.genre_biography),
            context.getString(R.string.genre_history),
            context.getString(R.string.genre_self_help),
            context.getString(R.string.genre_other)
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

    fun enumToDisplayString(context: Context, enumName: String): String {
        return when (enumName) {
            Genre.FICTION.name -> context.getString(R.string.genre_fiction)
            Genre.NON_FICTION.name -> context.getString(R.string.genre_non_fiction)
            Genre.MYSTERY.name -> context.getString(R.string.genre_mystery)
            Genre.ROMANCE.name -> context.getString(R.string.genre_romance)
            Genre.SCIENCE_FICTION.name -> context.getString(R.string.genre_science_fiction)
            Genre.FANTASY.name -> context.getString(R.string.genre_fantasy)
            Genre.BIOGRAPHY.name -> context.getString(R.string.genre_biography)
            Genre.HISTORY.name -> context.getString(R.string.genre_history)
            Genre.SELF_HELP.name -> context.getString(R.string.genre_self_help)
            Genre.OTHER.name -> context.getString(R.string.genre_other)
            else -> context.getString(R.string.genre_other)
        }
    }
}