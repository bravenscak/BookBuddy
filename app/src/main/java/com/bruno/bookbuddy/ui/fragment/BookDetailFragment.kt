package com.bruno.bookbuddy.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.appcompat.app.AlertDialog
import com.bruno.bookbuddy.R
import com.bruno.bookbuddy.databinding.FragmentBookDetailBinding
import com.bruno.bookbuddy.data.model.Book
import com.bruno.bookbuddy.data.model.ReadingStatus
import com.bruno.bookbuddy.utils.getBookByIdFromProvider
import com.bruno.bookbuddy.utils.deleteBookViaProvider
import java.io.File

class BookDetailFragment : Fragment() {

    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private var bookId: Long = 0
    private lateinit var book: Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookId = arguments?.getLong("bookId") ?: 0
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
        setupMenu()
        loadBookDetails()
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.book_detail_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit -> {
                        navigateToEditBook()
                        true
                    }
                    R.id.action_delete -> {
                        showDeleteConfirmation()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun navigateToEditBook() {
        val bundle = Bundle().apply {
            putLong("bookId", bookId)
        }
        findNavController().navigate(R.id.action_bookDetail_to_editBook, bundle)
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.delete))
            setMessage(getString(R.string.sure_to_delete, book.title))
            setIcon(R.drawable.ic_delete)
            setCancelable(true)
            setPositiveButton(android.R.string.ok) { _, _ ->
                deleteBook()
            }
            setNegativeButton(getString(R.string.cancel), null)
            show()
        }
    }

    private fun deleteBook() {
        val rowsDeleted = requireContext().deleteBookViaProvider(bookId)

        if (rowsDeleted > 0) {
            if (book.coverPath.isNotEmpty()) {
                File(book.coverPath).delete()
            }
            findNavController().navigateUp()
        } else {
            showErrorDialog("Failed to delete book")
        }
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Error")
            setMessage(message)
            setPositiveButton(android.R.string.ok, null)
            show()
        }
    }

    private fun loadBookDetails() {
        val loadedBook = requireContext().getBookByIdFromProvider(bookId)

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
}