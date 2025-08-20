package com.bruno.bookbuddy.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bruno.bookbuddy.R
import com.bruno.bookbuddy.databinding.FragmentBookListBinding
import com.bruno.bookbuddy.data.model.Book
import com.bruno.bookbuddy.data.repository.getBookRepository
import com.bruno.bookbuddy.ui.adapter.BookAdapter

class BookListFragment : Fragment() {

    private var _binding: FragmentBookListBinding? = null
    private val binding get() = _binding!!

    private lateinit var bookAdapter: BookAdapter
    private val books = mutableListOf<Book>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadBooks()
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(
            context = requireContext(),
            books = books,
            onBookClick = { book, position ->
                navigateToBookDetail(book)
            }
        )

        binding.rvBooks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bookAdapter
            setHasFixedSize(true)
        }
    }

    private fun navigateToBookDetail(book: Book) {
        book._id?.let { bookId ->
            val bundle = Bundle().apply {
                putLong("bookId", bookId)
            }
            findNavController().navigate(R.id.bookDetailFragment, bundle)
        }
    }

    private fun loadBooks() {
        val repository = getBookRepository(requireContext())
        val bookList = repository.getAllBooks()

        if (bookList.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
            bookAdapter.updateBooks(bookList)
        }
    }

    private fun showEmptyState() {
        binding.rvBooks.visibility = View.GONE
        binding.tvEmptyState.visibility = View.VISIBLE
        binding.tvEmptySubtitle.visibility = View.VISIBLE
    }

    private fun hideEmptyState() {
        binding.rvBooks.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE
        binding.tvEmptySubtitle.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        loadBooks()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}