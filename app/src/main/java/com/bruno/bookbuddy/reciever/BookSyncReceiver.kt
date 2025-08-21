package com.bruno.bookbuddy.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bruno.bookbuddy.MainActivity
import com.bruno.bookbuddy.R

class BookSyncReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SYNC_COMPLETE -> handleSyncComplete(context, intent)
            ACTION_BOOK_ADDED -> handleBookAdded(context, intent)
            ACTION_READING_REMINDER -> handleReadingReminder(context)
        }
    }

    private fun handleSyncComplete(context: Context, intent: Intent) {
        val booksAdded = intent.getIntExtra(EXTRA_BOOKS_ADDED, 0)
        val timestamp = intent.getLongExtra(EXTRA_TIMESTAMP, System.currentTimeMillis())

        if (booksAdded > 0) {
            showNotification(
                context,
                "New Books Available",
                "$booksAdded new books added to your library",
                NOTIFICATION_ID_SYNC,
                createRefreshIntent(context)
            )
        }

        updateLastSyncTime(context, timestamp)
    }

    private fun handleBookAdded(context: Context, intent: Intent) {
        val bookTitle = intent.getStringExtra(EXTRA_BOOK_TITLE) ?: "New Book"

        showNotification(
            context,
            "Book Added",
            "\"$bookTitle\" has been added to your library",
            NOTIFICATION_ID_BOOK_ADDED,
            createRefreshIntent(context)
        )
    }

    private fun handleReadingReminder(context: Context) {
        showNotification(
            context,
            "Time to Read! 📚",
            "You haven't opened BookBuddy in a while. Check out your collection!",
            NOTIFICATION_ID_REMINDER,
            createRefreshIntent(context)
        )
    }

    private fun createRefreshIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("refresh_from_notification", true)
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun showNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int,
        pendingIntent: PendingIntent
    ) {
        val notificationManager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) ?: return

        createNotificationChannel(notificationManager)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_book)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "BookBuddy",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "BookBuddy app notifications"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateLastSyncTime(context: Context, timestamp: Long) {
        val prefs = context.getSharedPreferences("book_buddy_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putLong("last_sync_time", timestamp)
            .apply()
    }

    companion object {
        const val ACTION_SYNC_COMPLETE = "com.bruno.bookbuddy.ACTION_SYNC_COMPLETE"
        const val ACTION_BOOK_ADDED = "com.bruno.bookbuddy.ACTION_BOOK_ADDED"
        const val ACTION_READING_REMINDER = "com.bruno.bookbuddy.ACTION_READING_REMINDER"

        const val EXTRA_BOOKS_ADDED = "books_added"
        const val EXTRA_TIMESTAMP = "timestamp"
        const val EXTRA_BOOK_TITLE = "book_title"

        private const val CHANNEL_ID = "bookbuddy_notifications"
        private const val NOTIFICATION_ID_SYNC = 1001
        private const val NOTIFICATION_ID_BOOK_ADDED = 1002
        private const val NOTIFICATION_ID_REMINDER = 1003
    }
}