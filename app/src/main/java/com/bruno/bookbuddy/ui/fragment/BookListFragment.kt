package com.bruno.bookbuddy.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.bruno.bookbuddy.R
import com.bruno.bookbuddy.databinding.FragmentBookListBinding
import com.bruno.bookbuddy.data.model.Book
import com.bruno.bookbuddy.network.worker.BookSyncWorker
import com.bruno.bookbuddy.ui.adapter.BookAdapter
import com.bruno.bookbuddy.utils.fetchBooksFromProvider

class BookListFragment : Fragment() {

    private var _binding: FragmentBookListBinding? = null
    private val binding get() = _binding!!

    private lateinit var bookAdapter: BookAdapter
    private val books = mutableListOf<Book>()

    // Pull-to-refresh timeout handler
    private val refreshTimeoutHandler = Handler(Looper.getMainLooper())
    private var refreshTimeoutRunnable: Runnable? = null
    private val REFRESH_TIMEOUT_MS = 8000L // 8 seconds max loading

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
        setupSwipeRefresh()
        loadBooks()
        handleNotificationIntent()
    }

    private fun handleNotificationIntent() {
        if (activity?.intent?.getBooleanExtra("refresh_from_notification", false) == true) {
            loadBooks()
            activity?.intent?.removeExtra("refresh_from_notification")
        }
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

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshBooks()
        }

        // Material Design color scheme
        binding.swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
    }

    private fun refreshBooks() {
        // Set timeout for refresh
        refreshTimeoutRunnable = Runnable {
            if (binding.swipeRefreshLayout.isRefreshing) {
                binding.swipeRefreshLayout.isRefreshing = false
                // Could show a timeout message here if needed
            }
        }
        refreshTimeoutHandler.postDelayed(refreshTimeoutRunnable!!, REFRESH_TIMEOUT_MS)

        // Start background sync work
        val syncWorkRequest = OneTimeWorkRequest.Builder(BookSyncWorker::class.java)
            .addTag(BookSyncWorker.TAG_POPULAR_SYNC)
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniqueWork(
            BookSyncWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )

        // Monitor work completion
        WorkManager.getInstance(requireContext())
            .getWorkInfoByIdLiveData(syncWorkRequest.id)
            .observe(viewLifecycleOwner) { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    // Cancel timeout
                    refreshTimeoutRunnable?.let {
                        refreshTimeoutHandler.removeCallbacks(it)
                    }

                    // Stop refresh animation
                    if (binding.swipeRefreshLayout.isRefreshing) {
                        binding.swipeRefreshLayout.isRefreshing = false
                    }

                    // Reload books from database
                    loadBooks()
                }
            }
    }

    private fun navigateToBookDetail(book: Book) {
        book._id?.let { bookId ->
            val bundle = Bundle().apply {
                putLong("bookId", bookId)
            }
            findNavController().navigate(R.id.action_bookList_to_bookDetail, bundle)
        }
    }

    private fun loadBooks() {
        val bookList = requireContext().fetchBooksFromProvider()

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
        // Clean up timeout handler
        refreshTimeoutRunnable?.let {
            refreshTimeoutHandler.removeCallbacks(it)
        }
        _binding = null
    }
}