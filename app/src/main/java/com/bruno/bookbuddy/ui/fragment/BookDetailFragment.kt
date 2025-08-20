package com.bruno.bookbuddy.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bruno.bookbuddy.R
import com.bruno.bookbuddy.databinding.FragmentBookDetailBinding
import com.bruno.bookbuddy.data.model.Book
import com.bruno.bookbuddy.data.model.ReadingStatus
import com.bruno.bookbuddy.data.repository.getBookRepository
import java.io.File

class BookDetailFragment : Fragment() {

    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private var bookId: Long = 0
    private lateinit var book: Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bookId = it.getLong(ARG_BOOK_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadBookDetails()
    }

    private fun loadBookDetails() {
        val repository = getBookRepository(requireContext())
        val loadedBook = repository.getBookById(bookId)

        if (loadedBook != null) {
            book = loadedBook
            displayBookDetails()
        } else {
            binding.tvBookTitle.text = "Book not found"
            binding.tvBookAuthor.text = "Error loading book details"
        }
    }

    private fun displayBookDetails() {
        binding.apply {
            tvBookTitle.text = book.title
            tvBookAuthor.text = book.author
            tvBookYear.text = book.year.toString()
            tvBookGenre.text = book.genre
            tvBookExplanation.text = "Added on ${book.createdAt.substring(0, 10)}"

            tvBookRating.text = if (book.rating > 0) {
                String.format("%.1f", book.rating)
            } else {
                "Not rated"
            }

            chipBookStatus.text = when (book.status) {
                ReadingStatus.WANT_TO_READ -> "Want to Read"
                ReadingStatus.CURRENTLY_READING -> "Currently Reading"
                ReadingStatus.FINISHED -> "Finished"
                ReadingStatus.ABANDONED -> "Abandoned"
            }

            chipBookStatus.setChipBackgroundColorResource(
                when (book.status) {
                    ReadingStatus.WANT_TO_READ -> R.color.status_want_to_read
                    ReadingStatus.CURRENTLY_READING -> R.color.status_currently_reading
                    ReadingStatus.FINISHED -> R.color.status_finished
                    ReadingStatus.ABANDONED -> R.color.status_abandoned
                }
            )

            loadBookCover()
        }
    }

    private fun loadBookCover() {
        if (book.coverPath.isNotEmpty() && File(book.coverPath).exists()) {
            binding.ivBookCover.setImageURI(android.net.Uri.fromFile(File(book.coverPath)))
        } else {
            binding.ivBookCover.setImageResource(R.drawable.ic_book)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_BOOK_ID = "bookId"

        fun newInstance(bookId: Long) = BookDetailFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_BOOK_ID, bookId)
            }
        }
    }
}