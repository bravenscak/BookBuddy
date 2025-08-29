package com.bruno.bookbuddy.ui.fragment

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bruno.bookbuddy.R
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
import com.bruno.bookbuddy.network.model.getCoverUrl
import com.bruno.bookbuddy.network.model.getMainAuthor
import com.bruno.bookbuddy.network.model.getMainGenre
import com.bruno.bookbuddy.network.model.getPublishYear
import com.bruno.bookbuddy.utils.GenreUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AddBookFragment : Fragment() {

    private var _binding: FragmentAddBookBinding? = null
    private val binding get() = _binding!!

    private var bookId: Long = -1
    private var existingBook: Book? = null
    private val isEditMode get() = bookId != -1L

    private lateinit var bookFetcher: BookFetcher

    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivBookCover.setImageURI(it)
            binding.btnAddCover.text = getString(R.string.change_cover)
        }
    }

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
        val genres = GenreUtils.getAllDisplayNames(requireContext())

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
                ReadingStatus.WANT_TO_READ -> getString(R.string.status_want_to_read)
                ReadingStatus.CURRENTLY_READING -> getString(R.string.status_currently_reading)
                ReadingStatus.FINISHED -> getString(R.string.status_finished)
                ReadingStatus.ABANDONED -> getString(R.string.status_abandoned)
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
                getString(R.string.not_rated)
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
            imagePickerLauncher.launch("image/*")
        }

        binding.btnSearchApi.setOnClickListener {
            searchBooksOnline()
        }
    }

    private fun searchBooksOnline() {
        val title = binding.etTitle.text.toString().trim()
        val author = binding.etAuthor.text.toString().trim()

        if (title.isEmpty() && author.isEmpty()) {
            binding.tilTitle.error = getString(R.string.enter_title_or_author)
            return
        }

        binding.btnSearchApi.isEnabled = false
        binding.btnSearchApi.text = getString(R.string.searching)

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
        binding.btnSearchApi.text = getString(R.string.search_online)

        if (books.isNotEmpty()) {
            val firstBook = books.first()
            populateFormFromApi(firstBook)

            binding.tilTitle.error = null
            binding.tilAuthor.error = null
        } else {
            binding.tilTitle.error = getString(R.string.no_books_found_search)
        }
    }

    private fun handleSearchError(error: String) {
        binding.btnSearchApi.isEnabled = true
        binding.btnSearchApi.text = getString(R.string.search_online)

        binding.tilTitle.error = "${getString(R.string.search_failed)}: $error"
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

        googleBook.getCoverUrl()?.let { coverUrl ->
            downloadAndSetCover(coverUrl)
        }
    }

    private fun downloadAndSetCover(coverUrl: String) {
        binding.btnAddCover.text = "Downloading..."
        binding.btnAddCover.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = java.net.URL(coverUrl)
                val connection = url.openConnection()
                val inputStream = connection.getInputStream()

                val filename = "api_cover_${System.currentTimeMillis()}.jpg"
                val file = File(requireContext().getExternalFilesDir(null), filename)
                val outputStream = FileOutputStream(file)

                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()

                launch(Dispatchers.Main) {
                    val uri = Uri.fromFile(file)
                    selectedImageUri = uri
                    binding.ivBookCover.setImageURI(uri)
                    binding.btnAddCover.text = getString(R.string.change_cover)
                    binding.btnAddCover.isEnabled = true
                }

            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    binding.btnAddCover.text = getString(R.string.add_cover)
                    binding.btnAddCover.isEnabled = true
                }
            }
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
            showError(getString(R.string.failed_to_save_book))
        }
    }

    private fun updateExistingBook() {
        existingBook?.let { book ->
            val newCoverPath = selectedImageUri?.let { uri ->
                saveCoverImage(uri)
            } ?: book.coverPath

            val updatedBook = book.copy(
                title = binding.etTitle.text.toString().trim(),
                author = binding.etAuthor.text.toString().trim(),
                year = binding.etYear.text.toString().toInt(),
                genre = getSelectedGenre(),
                status = getSelectedStatus(),
                rating = binding.rbRating.rating,
                coverPath = newCoverPath
            )

            val rowsUpdated = requireContext().updateBookViaProvider(updatedBook)

            if (rowsUpdated > 0) {
                findNavController().navigateUp()
            } else {
                showError(getString(R.string.failed_to_update_book))
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        val title = binding.etTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.tilTitle.error = getString(R.string.title_required)
            isValid = false
        } else {
            binding.tilTitle.error = null
        }

        val author = binding.etAuthor.text.toString().trim()
        if (author.isEmpty()) {
            binding.tilAuthor.error = getString(R.string.author_required)
            isValid = false
        } else {
            binding.tilAuthor.error = null
        }

        val yearText = binding.etYear.text.toString().trim()
        if (yearText.isEmpty()) {
            binding.tilYear.error = getString(R.string.year_required)
            isValid = false
        } else {
            val year = yearText.toIntOrNull()
            val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

            if (year == null || year < 1000 || year > currentYear + 1) {
                binding.tilYear.error = getString(R.string.enter_valid_year) + " (1000-${currentYear + 1})"
                isValid = false
            } else {
                binding.tilYear.error = null
            }
        }

        if (binding.actvGenre.text.toString().isEmpty()) {
            binding.tilGenre.error = getString(R.string.genre_required)
            isValid = false
        } else {
            binding.tilGenre.error = null
        }

        if (binding.actvStatus.text.toString().isEmpty()) {
            binding.tilStatus.error = getString(R.string.status_required)
            isValid = false
        } else {
            binding.tilStatus.error = null
        }

        return isValid
    }

    private fun createBookFromForm(): Book {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = dateFormat.format(Date())

        val coverPath = selectedImageUri?.let { uri ->
            saveCoverImage(uri)
        } ?: ""

        return Book(
            _id = null,
            title = binding.etTitle.text.toString().trim(),
            author = binding.etAuthor.text.toString().trim(),
            year = binding.etYear.text.toString().toInt(),
            genre = getSelectedGenre(),
            status = getSelectedStatus(),
            rating = binding.rbRating.rating,
            coverPath = coverPath,
            createdAt = currentTime
        )
    }

    private fun saveCoverImage(uri: Uri): String {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val filename = "book_cover_${System.currentTimeMillis()}.jpg"
            val file = File(requireContext().getExternalFilesDir(null), filename)

            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            file.absolutePath
        } catch (e: Exception) {
            ""
        }
    }

    private fun getSelectedGenre(): String {
        val selectedText = binding.actvGenre.text.toString()
        return GenreUtils.displayToEnum(selectedText)
    }

    private fun getSelectedStatus(): ReadingStatus {
        val statusText = binding.actvStatus.text.toString()
        val wantToRead = getString(R.string.status_want_to_read)
        val currentlyReading = getString(R.string.status_currently_reading)
        val finished = getString(R.string.status_finished)
        val abandoned = getString(R.string.status_abandoned)

        return when (statusText) {
            wantToRead -> ReadingStatus.WANT_TO_READ
            currentlyReading -> ReadingStatus.CURRENTLY_READING
            finished -> ReadingStatus.FINISHED
            abandoned -> ReadingStatus.ABANDONED
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

        val genreDisplayName = GenreUtils.enumToDisplayString(requireContext(), book.genre)
        binding.actvGenre.setText(genreDisplayName, false)

        val statusDisplayName = getStatusDisplayName(book.status)
        binding.actvStatus.setText(statusDisplayName, false)

        binding.rbRating.rating = book.rating

        if (book.coverPath.isNotEmpty()) {
            loadBookCover(book.coverPath)
        }
    }

    private fun updateUIForEditMode() {
        binding.btnSave.text = getString(R.string.update)
        binding.btnSearchApi.visibility = View.GONE
    }

    private fun loadBookCover(coverPath: String) {
        if (coverPath.isNotEmpty() && java.io.File(coverPath).exists()) {
            binding.ivBookCover.setImageURI(android.net.Uri.fromFile(java.io.File(coverPath)))
            binding.btnAddCover.text = getString(R.string.change_cover)
        }
    }

    private fun getStatusDisplayName(status: ReadingStatus): String {
        return when (status) {
            ReadingStatus.WANT_TO_READ -> getString(R.string.status_want_to_read)
            ReadingStatus.CURRENTLY_READING -> getString(R.string.status_currently_reading)
            ReadingStatus.FINISHED -> getString(R.string.status_finished)
            ReadingStatus.ABANDONED -> getString(R.string.status_abandoned)
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