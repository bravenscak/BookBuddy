package com.bruno.bookbuddy.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.bruno.bookbuddy.receiver.BookSyncReceiver
import java.util.Calendar

class ReadingReminderManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReadingReminder(intervalHours: Int = 24) {
        val intent = Intent(context, BookSyncReceiver::class.java).apply {
            action = BookSyncReceiver.ACTION_READING_REMINDER
        }

        val pendingIntent = createPendingIntent(intent, REMINDER_REQUEST_CODE)

        val triggerTime = System.currentTimeMillis() + (intervalHours * 60 * 60 * 1000)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelReadingReminders() {
        val intent = Intent(context, BookSyncReceiver::class.java)
        val pendingIntent = createPendingIntent(intent, REMINDER_REQUEST_CODE)

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun createPendingIntent(intent: Intent, requestCode: Int): PendingIntent {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }

    companion object {
        private const val REMINDER_REQUEST_CODE = 2001
    }
}