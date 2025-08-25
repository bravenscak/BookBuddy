package com.bruno.bookbuddy.utils

import android.content.Context
import com.bruno.bookbuddy.data.model.Book
import com.bruno.bookbuddy.data.model.ReadingStatus
import com.bruno.bookbuddy.data.model.Genre
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
                genre = Genre.FICTION.name,
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
                genre = Genre.FICTION.name,
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
                genre = Genre.SCIENCE_FICTION.name,
                status = ReadingStatus.CURRENTLY_READING,
                rating = 0f,
                coverPath = "",
                createdAt = currentTime
            ),
            Book(
                _id = null,
                title = "Dune",
                author = "Frank Herbert",
                year = 1965,
                genre = Genre.SCIENCE_FICTION.name,
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
                genre = Genre.FICTION.name,
                status = ReadingStatus.ABANDONED,
                rating = 2.5f,
                coverPath = "",
                createdAt = currentTime
            ),
            Book(
                _id = null,
                title = "Agatha Christie: An Autobiography",
                author = "Agatha Christie",
                year = 1977,
                genre = Genre.BIOGRAPHY.name,
                status = ReadingStatus.WANT_TO_READ,
                rating = 0f,
                coverPath = "",
                createdAt = currentTime
            ),
            Book(
                _id = null,
                title = "The Murder of Roger Ackroyd",
                author = "Agatha Christie",
                year = 1926,
                genre = Genre.MYSTERY.name,
                status = ReadingStatus.FINISHED,
                rating = 4.5f,
                coverPath = "",
                createdAt = currentTime
            ),
            Book(
                _id = null,
                title = "A Brief History of Time",
                author = "Stephen Hawking",
                year = 1988,
                genre = Genre.SCIENCE_FICTION.name,
                status = ReadingStatus.CURRENTLY_READING,
                rating = 4.0f,
                coverPath = "",
                createdAt = currentTime
            )
        )
    }
}