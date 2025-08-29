package com.bruno.bookbuddy.ui.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.bruno.bookbuddy.R
import com.bruno.bookbuddy.receiver.BookSyncReceiver
import com.bruno.bookbuddy.service.ReadingReminderManager
import com.bruno.bookbuddy.utils.LocaleHelper
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val currentLanguage = LocaleHelper.getCurrentLanguage(requireContext())
        val languagePref = findPreference<ListPreference>("language")
        if (languagePref?.value.isNullOrEmpty()) {
            languagePref?.value = currentLanguage
        }

        setupPreferences()
    }

    private fun setupPreferences() {
        val languagePref = findPreference<ListPreference>("language")
        languagePref?.setOnPreferenceChangeListener { _, newValue ->
            handleLanguageChange(newValue as String)
            true
        }

        val themePref = findPreference<ListPreference>("theme")
        themePref?.setOnPreferenceChangeListener { _, newValue ->
            handleThemeChange(newValue as String)
            true
        }

        val remindersPref = findPreference<SwitchPreferenceCompat>("reading_reminders")
        remindersPref?.setOnPreferenceChangeListener { _, newValue ->
            handleRemindersChange(newValue as Boolean)
            true
        }

        val frequencyPref = findPreference<ListPreference>("reminder_frequency")
        frequencyPref?.setOnPreferenceChangeListener { _, newValue ->
            handleFrequencyChange(newValue as String)
            true
        }
    }

    private fun handleLanguageChange(language: String) {
        LocaleHelper.saveLanguage(requireContext(), language)

        val message = if (language == "hr") {
            "Restartaj aplikaciju da se primijene promjene"
        } else {
            "Restart app to apply changes"
        }

        val snackbar = com.google.android.material.snackbar.Snackbar.make(
            requireView(),
            message,
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        )
        snackbar.setAction("RESTART") {
            val packageManager = requireContext().packageManager
            val intent = packageManager.getLaunchIntentForPackage(requireContext().packageName)
            val componentName = intent?.component
            val mainIntent = Intent.makeRestartActivityTask(componentName)
            requireActivity().startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
        }
        snackbar.show()
    }

    private fun handleThemeChange(theme: String) {
        preferenceManager.sharedPreferences?.edit()
            ?.putString("theme", theme)
            ?.apply()

        val message = when (theme) {
            "light" -> "Switched to light theme"
            "dark" -> "Switched to dark theme"
            "system" -> "Following system theme"
            else -> "Theme changed"
        }

        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()

        when (theme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun handleRemindersChange(enabled: Boolean) {
        preferenceManager.sharedPreferences?.edit()
            ?.putBoolean("reminders_enabled", enabled)
            ?.apply()

        if (enabled) {
            scheduleReadingReminders()
        } else {
            cancelReadingReminders()
        }
    }

    private fun handleFrequencyChange(frequency: String) {
        preferenceManager.sharedPreferences?.edit()
            ?.putString("reminder_frequency_hours", frequency)
            ?.apply()

        val remindersEnabled = preferenceManager.sharedPreferences
            ?.getBoolean("reminders_enabled", true) ?: true

        if (remindersEnabled) {
            scheduleReadingReminders()
        }
        testNotification()
    }

    private fun showRestartRequiredMessage() {
        val snackbar = com.google.android.material.snackbar.Snackbar.make(
            requireView(),
            "Restart app to apply changes",
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        )
        snackbar.show()
    }

    private fun scheduleReadingReminders() {
        val frequencyHours = preferenceManager.sharedPreferences
            ?.getString("reminder_frequency", "24")?.toIntOrNull() ?: 24

        val reminderManager = ReadingReminderManager(requireContext())
        reminderManager.scheduleReadingReminder(frequencyHours)
    }

    private fun cancelReadingReminders() {
        val reminderManager = ReadingReminderManager(requireContext())
        reminderManager.cancelReadingReminders()
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "language" -> {
                android.util.Log.d("SettingsFragment", "Language preference changed")
            }
            "theme" -> {
                android.util.Log.d("SettingsFragment", "Theme preference changed")
            }
        }
    }

    private fun testNotification() {
        val intent = Intent(requireContext(), BookSyncReceiver::class.java)
        intent.action = BookSyncReceiver.ACTION_READING_REMINDER
        requireContext().sendBroadcast(intent)
    }
}