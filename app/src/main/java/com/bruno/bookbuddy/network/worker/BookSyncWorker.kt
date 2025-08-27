package com.bruno.bookbuddy.network.worker

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.bruno.bookbuddy.network.service.BookFetcher
import com.bruno.bookbuddy.receiver.BookSyncReceiver
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class BookSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        if (!hasNetworkConnection()) {
            return Result.retry()
        }

        return try {
            val bookFetcher = BookFetcher(context)
            val latch = CountDownLatch(1)
            var booksAdded = 0

            val shouldReset = inputData.getBoolean(KEY_RESET_OFFSET, false)
            if (shouldReset) {
                bookFetcher.resetOffset()
            }

            bookFetcher.fetchPopularBooks { count ->
                booksAdded = count
                latch.countDown()
            }

            val completed = latch.await(15, TimeUnit.SECONDS)

            if (completed) {
                sendSyncCompleteBroadcast(booksAdded)
                Result.success()
            } else {
                Result.failure()
            }

        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun sendSyncCompleteBroadcast(booksAdded: Int) {
        val intent = Intent(context, BookSyncReceiver::class.java).apply {
            action = BookSyncReceiver.ACTION_SYNC_COMPLETE
            putExtra(BookSyncReceiver.EXTRA_BOOKS_ADDED, booksAdded)
            putExtra(BookSyncReceiver.EXTRA_TIMESTAMP, System.currentTimeMillis())
        }
        context.sendBroadcast(intent)
    }

    private fun hasNetworkConnection(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    companion object {
        const val WORK_NAME = "book_sync_work"
        const val TAG_POPULAR_SYNC = "popular_sync"
        const val KEY_RESET_OFFSET = "reset_offset"
    }
}