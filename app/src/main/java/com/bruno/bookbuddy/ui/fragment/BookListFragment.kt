package com.bruno.bookbuddy.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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

    private val refreshTimeoutHandler = Handler(Looper.getMainLooper())
    private var refreshTimeoutRunnable: Runnable? = null
    private val REFRESH_TIMEOUT_MS = 8000L

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
        } else {
        }
    }

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
            onBookClick = { bookId ->
                navigateToBookDetail(bookId)
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

        binding.swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
    }

    private fun refreshBooks() {
        refreshTimeoutRunnable = Runnable {
            if (binding.swipeRefreshLayout.isRefreshing) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
        refreshTimeoutHandler.postDelayed(refreshTimeoutRunnable!!, REFRESH_TIMEOUT_MS)

        val currentBooks = requireContext().fetchBooksFromProvider()
        val shouldReset = currentBooks.size <= 5

        val inputData = androidx.work.Data.Builder()
            .putBoolean(BookSyncWorker.KEY_RESET_OFFSET, shouldReset)
            .build()

        val syncWorkRequest = OneTimeWorkRequest.Builder(BookSyncWorker::class.java)
            .addTag(BookSyncWorker.TAG_POPULAR_SYNC)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(requireContext()).enqueueUniqueWork(
            BookSyncWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )

        WorkManager.getInstance(requireContext())
            .getWorkInfoByIdLiveData(syncWorkRequest.id)
            .observe(viewLifecycleOwner) { workInfo ->
                if (workInfo != null && workInfo.state.isFinished) {
                    refreshTimeoutRunnable?.let {
                        refreshTimeoutHandler.removeCallbacks(it)
                    }

                    if (binding.swipeRefreshLayout.isRefreshing) {
                        binding.swipeRefreshLayout.isRefreshing = false
                    }

                    loadBooks()
                }
            }
    }

    private fun navigateToBookDetail(bookId: Long) {
        val bundle = Bundle().apply {
            putLong("bookId", bookId)
        }
        findNavController().navigate(R.id.action_bookList_to_bookDetail, bundle)
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
        refreshTimeoutRunnable?.let {
            refreshTimeoutHandler.removeCallbacks(it)
        }
        _binding = null
    }
}