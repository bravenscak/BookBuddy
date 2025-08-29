package com.bruno.bookbuddy

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class BookBuddyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        applySelectedTheme()
    }

    private fun applySelectedTheme() {
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        val selectedTheme = prefs.getString("theme", "system")

        when (selectedTheme) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}