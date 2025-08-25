package com.bruno.bookbuddy.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.bruno.bookbuddy.databinding.FragmentAddBookBinding
import com.bruno.bookbuddy.data.model.Book
import com.bruno.bookbuddy.data.model.ReadingStatus
import com.bruno.bookbuddy.utils.insertBookViaProvider
import com.bruno.bookbuddy.utils.getBookByIdFromProvider
import com.bruno.bookbuddy.utils.updateBookViaProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.bruno.bookbuddy.network.service.BookFetcher
import com.bruno.bookbuddy.network.model.GoogleBookItem
import com.bruno.bookbuddy.network.model.getMainAuthor
import com.bruno.bookbuddy.network.model.getMainGenre
import com.bruno.bookbuddy.network.model.getPublishYear
import com.bruno.bookbuddy.utils.GenreUtils

class AddBookFragment : Fragment() {

    private var _binding: FragmentAddBookBinding? = null
    private val binding get() = _binding!!

    private var bookId: Long = -1
    private var existingBook: Book? = null
    private val isEditMode get() = bookId != -1L

    private lateinit var bookFetcher: BookFetcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bookId = it.getLong(ARG_BOOK_ID, -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookFetcher = BookFetcher(requireContext())

        setupDropdowns()
        setupRatingBar()
        setupClickListeners()

        if (isEditMode) {
            loadExistingBook()
            updateUIForEditMode()
        } else {
            binding.btnSearchApi.visibility = View.VISIBLE
        }
    }

    private fun setupDropdowns() {
        setupGenreDropdown()
        setupStatusDropdown()
    }

    private fun setupGenreDropdown() {
        val genres = GenreUtils.getAllDisplayNames()

        val genreAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            genres
        )
        binding.actvGenre.setAdapter(genreAdapter)
        binding.actvGenre.setText(genres[0], false)
    }

    private fun setupStatusDropdown() {
        val statuses = ReadingStatus.values().map { status ->
            when (status) {
                ReadingStatus.WANT_TO_READ -> "Want to Read"
                ReadingStatus.CURRENTLY_READING -> "Currently Reading"
                ReadingStatus.FINISHED -> "Finished"
                ReadingStatus.ABANDONED -> "Abandoned"
            }
        }

        val statusAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            statuses
        )
        binding.actvStatus.setAdapter(statusAdapter)
        binding.actvStatus.setText(statuses[0], false)
    }

    private fun setupRatingBar() {
        binding.rbRating.setOnRatingBarChangeListener { _, rating, _ ->
            binding.tvRatingValue.text = if (rating > 0) {
                String.format("%.1f", rating)
            } else {
                "Not rated"
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            saveBook()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnAddCover.setOnClickListener {

        }

        binding.btnSearchApi.setOnClickListener {
            searchBooksOnline()
        }
    }

    private fun searchBooksOnline() {
        val title = binding.etTitle.text.toString().trim()
        val author = binding.etAuthor.text.toString().trim()

        if (title.isEmpty() && author.isEmpty()) {
            binding.tilTitle.error = "Enter title or author to search"
            return
        }

        binding.btnSearchApi.isEnabled = false
        binding.btnSearchApi.text = "Searching..."

        bookFetcher.searchBooks(
            title = if (title.isNotEmpty()) title else null,
            author = if (author.isNotEmpty()) author else null,
            onSuccess = { books ->
                handleSearchResults(books)
            },
            onFailure = { error ->
                handleSearchError(error)
            }
        )
    }

    private fun handleSearchResults(books: List<GoogleBookItem>) {
        binding.btnSearchApi.isEnabled = true
        binding.btnSearchApi.text = "Search Online"

        Log.d("AddBookFragment", "=== SEARCH RESULTS ===")
        Log.d("AddBookFragment", "Found ${books.size} books")

        books.forEach { book ->
            Log.d("AddBookFragment", "Book: ${book.volumeInfo.title}")
            Log.d("AddBookFragment", "  Main genre: ${book.getMainGenre()}")
        }

        if (books.isNotEmpty()) {
            val firstBook = books.first()
            populateFormFromApi(firstBook)

            binding.tilTitle.error = null
            binding.tilAuthor.error = null
        } else {
            binding.tilTitle.error = "No books found"
        }
    }

    private fun handleSearchError(error: String) {
        binding.btnSearchApi.isEnabled = true
        binding.btnSearchApi.text = "Search Online"

        binding.tilTitle.error = "Search failed: $error"
    }

    private fun populateFormFromApi(googleBook: GoogleBookItem) {
        binding.etTitle.setText(googleBook.volumeInfo.title ?: "")
        binding.etAuthor.setText(googleBook.getMainAuthor())

        if (googleBook.getPublishYear() > 0) {
            binding.etYear.setText(googleBook.getPublishYear().toString())
        }

        val apiGenre = googleBook.getMainGenre()
        val enumGenre = GenreUtils.mapApiGenreToEnum(apiGenre)
        val displayGenre = GenreUtils.enumToDisplay(enumGenre)
        binding.actvGenre.setText(displayGenre, false)
    }

    private fun mapApiGenreToDisplayName(apiGenre: String): String {
        return when (apiGenre.uppercase()) {
            "SCIENCE", "SCIENCE_FICTION" -> "Science Fiction"
            "BIOGRAPHY" -> "Biography"
            "MYSTERY" -> "Mystery"
            "FANTASY" -> "Fantasy"
            "ROMANCE" -> "Romance"
            "HISTORY" -> "History"
            "FICTION" -> "Fiction"
            "OTHER" -> "Other"
            else -> "Fiction"
        }
    }

    private fun saveBook() {
        if (validateForm()) {
            if (isEditMode) {
                updateExistingBook()
            } else {
                createNewBook()
            }
        }
    }

    private fun createNewBook() {
        val book = createBookFromForm()

        val bookId = requireContext().insertBookViaProvider(book)

        if (bookId > 0) {
            findNavController().navigateUp()
        } else {
            showError("Failed to save book")
        }
    }

    private fun updateExistingBook() {
        existingBook?.let { book ->
            val updatedBook = book.copy(
                title = binding.etTitle.text.toString().trim(),
                author = binding.etAuthor.text.toString().trim(),
                year = binding.etYear.text.toString().toInt(),
                genre = getSelectedGenre(),
                status = getSelectedStatus(),
                rating = binding.rbRating.rating
            )

            val rowsUpdated = requireContext().updateBookViaProvider(updatedBook)

            if (rowsUpdated > 0) {
                findNavController().navigateUp()
            } else {
                showError("Failed to update book")
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val title = binding.etTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.tilTitle.error = "Title is required"
            isValid = false
        } else {
            binding.tilTitle.error = null
        }

        val author = binding.etAuthor.text.toString().trim()
        if (author.isEmpty()) {
            binding.tilAuthor.error = "Author is required"
            isValid = false
        } else {
            binding.tilAuthor.error = null
        }

        val yearText = binding.etYear.text.toString().trim()
        if (yearText.isEmpty()) {
            binding.tilYear.error = "Year is required"
            isValid = false
        } else {
            val year = yearText.toIntOrNull()
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

            if (year == null || year < 1000 || year > currentYear + 1) {
                binding.tilYear.error = "Enter valid year (1000-${currentYear + 1})"
                isValid = false
            } else {
                binding.tilYear.error = null
            }
        }

        if (binding.actvGenre.text.toString().isEmpty()) {
            binding.tilGenre.error = "Genre is required"
            isValid = false
        } else {
            binding.tilGenre.error = null
        }

        if (binding.actvStatus.text.toString().isEmpty()) {
            binding.tilStatus.error = "Status is required"
            isValid = false
        } else {
            binding.tilStatus.error = null
        }

        return isValid
    }

    private fun createBookFromForm(): Book {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = dateFormat.format(Date())

        return Book(
            _id = null,
            title = binding.etTitle.text.toString().trim(),
            author = binding.etAuthor.text.toString().trim(),
            year = binding.etYear.text.toString().toInt(),
            genre = getSelectedGenre(),
            status = getSelectedStatus(),
            rating = binding.rbRating.rating,
            coverPath = "",
            createdAt = currentTime
        )
    }

    private fun getSelectedGenre(): String {
        return GenreUtils.displayToEnum(binding.actvGenre.text.toString())
    }

    private fun getSelectedStatus(): ReadingStatus {
        return when (binding.actvStatus.text.toString()) {
            "Want to Read" -> ReadingStatus.WANT_TO_READ
            "Currently Reading" -> ReadingStatus.CURRENTLY_READING
            "Finished" -> ReadingStatus.FINISHED
            "Abandoned" -> ReadingStatus.ABANDONED
            else -> ReadingStatus.WANT_TO_READ
        }
    }

    private fun showError(message: String) {
        binding.tilTitle.error = message
    }

    private fun loadExistingBook() {
        existingBook = requireContext().getBookByIdFromProvider(bookId)
        existingBook?.let { book ->
            populateFormWithBook(book)
        }
    }

    private fun populateFormWithBook(book: Book) {
        binding.etTitle.setText(book.title)
        binding.etAuthor.setText(book.author)
        binding.etYear.setText(book.year.toString())

        val genreDisplayName = getGenreDisplayName(book.genre)
        binding.actvGenre.setText(genreDisplayName, false)

        val statusDisplayName = getStatusDisplayName(book.status)
        binding.actvStatus.setText(statusDisplayName, false)

        binding.rbRating.rating = book.rating

        if (book.coverPath.isNotEmpty()) {
            loadBookCover(book.coverPath)
        }
    }

    private fun updateUIForEditMode() {
        binding.btnSave.text = "Update"
        binding.btnSearchApi.visibility = View.GONE
    }

    private fun loadBookCover(coverPath: String) {
        if (coverPath.isNotEmpty() && java.io.File(coverPath).exists()) {
            binding.ivBookCover.setImageURI(android.net.Uri.fromFile(java.io.File(coverPath)))
        }
    }

    private fun getGenreDisplayName(genreEnum: String): String {
        return GenreUtils.enumToDisplay(genreEnum)
    }

    private fun getStatusDisplayName(status: ReadingStatus): String {
        return when (status) {
            ReadingStatus.WANT_TO_READ -> "Want to Read"
            ReadingStatus.CURRENTLY_READING -> "Currently Reading"
            ReadingStatus.FINISHED -> "Finished"
            ReadingStatus.ABANDONED -> "Abandoned"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_BOOK_ID = "bookId"

        fun newInstance(bookId: Long) = AddBookFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_BOOK_ID, bookId)
            }
        }
    }
}