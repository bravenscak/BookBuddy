package com.bruno.bookbuddy.network.service

import android.content.Context
import com.bruno.bookbuddy.data.model.Book
import com.bruno.bookbuddy.data.model.ReadingStatus
import com.bruno.bookbuddy.network.api.BookApi
import com.bruno.bookbuddy.network.api.GOOGLE_BOOKS_BASE_URL
import com.bruno.bookbuddy.network.model.GoogleBookItem
import com.bruno.bookbuddy.network.model.GoogleBookSearchResponse
import com.bruno.bookbuddy.network.model.getMainAuthor
import com.bruno.bookbuddy.network.model.getPublishYear
import com.bruno.bookbuddy.network.model.getCoverUrl
import com.bruno.bookbuddy.network.model.getMainGenre
import com.bruno.bookbuddy.utils.fetchBooksFromProvider
import com.bruno.bookbuddy.utils.insertBookViaProvider
import com.bruno.bookbuddy.utils.GenreUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookFetcher(private val context: Context) {

    private var bookApi: BookApi
    private var currentOffset = 0

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(GOOGLE_BOOKS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        bookApi = retrofit.create(BookApi::class.java)
    }

    fun searchBooks(
        title: String? = null,
        author: String? = null,
        onSuccess: (List<GoogleBookItem>) -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        val query = buildString {
            if (!title.isNullOrBlank()) {
                append("intitle:\"$title\"")
            }
            if (!author.isNullOrBlank()) {
                if (isNotEmpty()) append("+")
                append("inauthor:\"$author\"")
            }
            if (isEmpty()) {
                append("$title $author".trim())
            }
        }

        val request = bookApi.searchBooks(query, 10)

        request.enqueue(object : Callback<GoogleBookSearchResponse> {
            override fun onResponse(call: Call<GoogleBookSearchResponse>, response: Response<GoogleBookSearchResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let { searchResponse ->
                        onSuccess(searchResponse.items ?: emptyList())
                    } ?: onFailure("Empty response")
                } else {
                    onFailure("API Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<GoogleBookSearchResponse>, t: Throwable) {
                onFailure(t.message ?: "Network error")
            }
        })
    }

    fun fetchPopularBooks(onComplete: (Int) -> Unit = {}) {
        val request = bookApi.searchBooks("fiction", 20)

        request.enqueue(object : Callback<GoogleBookSearchResponse> {
            override fun onResponse(call: Call<GoogleBookSearchResponse>, response: Response<GoogleBookSearchResponse>) {
                if (response.isSuccessful) {
                    val books = response.body()?.items ?: emptyList()
                    val booksToAdd = books
                        .filter { it.volumeInfo.title != null && it.getMainAuthor() != "Unknown Author" }
                        .shuffled()
                        .take(5)

                    addBooks(booksToAdd, onComplete)
                } else {
                    onComplete(0)
                }
            }

            override fun onFailure(call: Call<GoogleBookSearchResponse>, t: Throwable) {
                onComplete(0)
            }
        })
    }

    fun resetOffset() {
        currentOffset = 0
    }

    private fun addBooks(books: List<GoogleBookItem>, onComplete: (Int) -> Unit) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val currentTime = dateFormat.format(Date())
            var booksAdded = 0

            books.forEach { googleBook ->
                val title = googleBook.volumeInfo.title!!
                val author = googleBook.getMainAuthor()

                if (!isDuplicateBook(title, author)) {
                    val apiGenre = googleBook.getMainGenre()
                    val enumGenre = GenreUtils.mapApiGenreToEnum(apiGenre)

                    val book = Book(
                        _id = null,
                        title = title,
                        author = author,
                        year = googleBook.getPublishYear(),
                        genre = enumGenre,
                        status = ReadingStatus.WANT_TO_READ,
                        rating = 0f,
                        coverPath = "",
                        createdAt = currentTime
                    )

                    val insertedId = context.insertBookViaProvider(book)
                    if (insertedId > 0) booksAdded++
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                onComplete(booksAdded)
            }
        }
    }

    private fun isDuplicateBook(title: String, author: String): Boolean {
        return try {
            val existingBooks = context.fetchBooksFromProvider()
            existingBooks.any { book ->
                book.title.equals(title, ignoreCase = true) &&
                        book.author.equals(author, ignoreCase = true)
            }
        } catch (e: Exception) {
            false
        }
    }

    fun convertGoogleBookToBook(googleBook: GoogleBookItem): Book {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = dateFormat.format(Date())
        val apiGenre = googleBook.getMainGenre()
        val enumGenre = GenreUtils.mapApiGenreToEnum(apiGenre)

        return Book(
            _id = null,
            title = googleBook.volumeInfo.title ?: "Unknown Title",
            author = googleBook.getMainAuthor(),
            year = googleBook.getPublishYear(),
            genre = enumGenre,
            status = ReadingStatus.WANT_TO_READ,
            rating = 0f,
            coverPath = googleBook.getCoverUrl() ?: "",
            createdAt = currentTime
        )
    }
}