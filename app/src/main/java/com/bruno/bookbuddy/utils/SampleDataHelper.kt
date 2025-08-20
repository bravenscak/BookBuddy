package com.bruno.bookbuddy.utils

import android.content.Context
import com.bruno.bookbuddy.data.model.Book
import com.bruno.bookbuddy.data.model.ReadingStatus
import com.bruno.bookbuddy.data.repository.getBookRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SampleDataHelper {

    fun addSampleBooksIfEmpty(context: Context) {
        val repository = getBookRepository(context)
        val existingBooks = repository.getAllBooks()

        if (existingBooks.isEmpty()) {
            val sampleBooks = createSampleBooks()
            sampleBooks.forEach { book ->
                repository.insertBook(book)
            }
        }
    }

    private fun createSampleBooks(): List<Book> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = dateFormat.format(Date())

        return listOf(
            Book(
                _id = null,
                title = "The Great Gatsby",
                author = "F. Scott Fitzgerald",
                year = 1925,
                genre = "Fiction",
                status = ReadingStatus.FINISHED,
                rating = 4.2f,
                coverPath = "",
                createdAt = currentTime
            ),
            Book(
                _id = null,
                title = "To Kill a Mockingbird",
                author = "Harper Lee",
                year = 1960,
                genre = "Fiction",
                status = ReadingStatus.FINISHED,
                rating = 4.8f,
                coverPath = "",
                createdAt = currentTime
            ),
            Book(
                _id = null,
                title = "1984",
                author = "George Orwell",
                year = 1949,
                genre = "Science Fiction",
                status = ReadingStatus.CURRENTLY_READING,
                rating = 0f,
                coverPath = "",
                createdAt = currentTime
            ),
            Book(
                _id = null,
                title = "Pride and Prejudice",
                author = "Jane Austen",
                year = 1813,
                genre = "Romance",
                status = ReadingStatus.WANT_TO_READ,
                rating = 0f,
                coverPath = "",
                createdAt = currentTime
            ),
            Book(
                _id = null,
                title = "The Catcher in the Rye",
                author = "J.D. Salinger",
                year = 1951,
                genre = "Fiction",
                status = ReadingStatus.ABANDONED,
                rating = 2.5f,
                coverPath = "",
                createdAt = currentTime
            )
        )
    }
}