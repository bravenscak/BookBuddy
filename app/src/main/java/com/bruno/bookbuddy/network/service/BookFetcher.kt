package com.bruno.bookbuddy.network.service

import android.content.Context
import com.bruno.bookbuddy.data.model.Book
import com.bruno.bookbuddy.data.model.ReadingStatus
import com.bruno.bookbuddy.network.api.BookApi
import com.bruno.bookbuddy.network.api.OPEN_LIBRARY_BASE_URL
import com.bruno.bookbuddy.network.model.BookApiItem
import com.bruno.bookbuddy.network.model.BookSearchResponse
import com.bruno.bookbuddy.utils.fetchBooksFromProvider
import com.bruno.bookbuddy.utils.insertBookViaProvider
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
import com.bruno.bookbuddy.data.model.Genre
import com.bruno.bookbuddy.utils.GenreUtils
import java.util.Locale

class BookFetcher(private val context: Context) {

    private var bookApi: BookApi

    private var currentOffset = 0

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(OPEN_LIBRARY_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        bookApi = retrofit.create(BookApi::class.java)
    }

    fun searchBooks(
        title: String? = null,
        author: String? = null,
        onSuccess: (List<BookApiItem>) -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        val request = bookApi.searchBooks(title, author, 10)

        request.enqueue(object : Callback<BookSearchResponse> {
            override fun onResponse(
                call: Call<BookSearchResponse>,
                response: Response<BookSearchResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { searchResponse ->
                        onSuccess(searchResponse.docs)
                    } ?: onFailure("Empty response")
                } else {
                    onFailure("API Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<BookSearchResponse>, t: Throwable) {
                onFailure(t.message ?: "Network error")
            }
        })
    }

    fun fetchPopularBooks(onComplete: (Int) -> Unit = {}) {
        val searchTerms = listOf(
            "bestseller",
            "classic novel",
            "science book",
            "biography famous",
            "mystery novel",
            "fantasy book",
            "romance novel",
            "history book"
        )

        val randomTerms = searchTerms.shuffled().take(5)
        var totalAdded = 0
        var completed = 0

        randomTerms.forEach { term ->
            searchWithOffset(term) { count ->
                totalAdded += count
                completed++

                if (completed >= randomTerms.size) {
                    if (totalAdded > 0) {
                        currentOffset += 2
                    }
                    onComplete(totalAdded)
                }
            }
        }
    }

    fun resetOffset() {
        currentOffset = 0
    }

    private fun searchWithOffset(term: String, onResult: (Int) -> Unit) {
        val request = bookApi.searchBooksWithOffset(term, 5, currentOffset) // Smanji limit

        request.enqueue(object : Callback<BookSearchResponse> {
            override fun onResponse(
                call: Call<BookSearchResponse>,
                response: Response<BookSearchResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { searchResponse ->
                        val booksToAdd = searchResponse.docs.take(1) // Samo 1 knjiga po termu
                        addBooksFromResponse(booksToAdd, term, onResult)
                    } ?: onResult(0)
                } else {
                    onResult(0)
                }
            }

            override fun onFailure(call: Call<BookSearchResponse>, t: Throwable) {
                onResult(0)
            }
        })
    }

    private fun addBooksFromResponse(apiItems: List<BookApiItem>?, searchTerm: String, onComplete: (Int) -> Unit) {
        if (apiItems.isNullOrEmpty()) {
            onComplete(0)
            return
        }

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val currentTime = dateFormat.format(Date())
            var booksAdded = 0

            apiItems.forEach { apiItem ->
                if (apiItem.title != null &&
                    apiItem.getMainAuthor() != "Unknown Author" &&
                    !isDuplicateBook(apiItem.title, apiItem.getMainAuthor())) {

                    val genre = getGenreFromSearchTerm(searchTerm, apiItem)

                    val book = Book(
                        _id = null,
                        title = apiItem.title,
                        author = apiItem.getMainAuthor(),
                        year = apiItem.getPublishYear(),
                        genre = genre,
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

    private fun getGenreFromSearchTerm(searchTerm: String, apiItem: BookApiItem): String {
        val apiGenre = apiItem.getMainGenre()

        val mappedFromApi = GenreUtils.mapApiGenreToEnum(apiGenre)
        if (mappedFromApi != Genre.FICTION.name && mappedFromApi != Genre.OTHER.name) {
            return mappedFromApi
        }

        return when {
            searchTerm.contains("science", ignoreCase = true) -> Genre.SCIENCE_FICTION.name
            searchTerm.contains("biography", ignoreCase = true) -> Genre.BIOGRAPHY.name
            searchTerm.contains("mystery", ignoreCase = true) -> Genre.MYSTERY.name
            searchTerm.contains("classic", ignoreCase = true) -> Genre.FICTION.name
            searchTerm.contains("bestseller", ignoreCase = true) -> Genre.FICTION.name
            searchTerm.contains("fantasy", ignoreCase = true) -> Genre.FANTASY.name
            searchTerm.contains("romance", ignoreCase = true) -> Genre.ROMANCE.name
            searchTerm.contains("history", ignoreCase = true) -> Genre.HISTORY.name
            else -> Genre.OTHER.name
        }
    }

    private fun mapToEnumGenre(apiGenre: String): String {
        return when (apiGenre.uppercase()) {
            "SCIENCE" -> "SCIENCE_FICTION"
            "BIOGRAPHY" -> "BIOGRAPHY"
            "MYSTERY" -> "MYSTERY"
            "FANTASY" -> "FANTASY"
            "ROMANCE" -> "ROMANCE"
            "HISTORY" -> "HISTORY"
            "FICTION" -> "FICTION"
            else -> "OTHER"
        }
    }

    fun convertApiItemToBook(apiItem: BookApiItem): Book {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = dateFormat.format(Date())

        return Book(
            _id = null,
            title = apiItem.title ?: "Unknown Title",
            author = apiItem.getMainAuthor(),
            year = apiItem.getPublishYear(),
            genre = apiItem.getMainGenre(),
            status = ReadingStatus.WANT_TO_READ,
            rating = 0f,
            coverPath = apiItem.getCoverUrl() ?: "",
            createdAt = currentTime
        )
    }
}