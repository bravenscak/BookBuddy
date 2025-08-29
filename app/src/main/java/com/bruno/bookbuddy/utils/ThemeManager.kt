package com.bruno.bookbuddy.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {

    private const val THEME_PREF = "selected_theme"
    private const val THEME_LIGHT = "light"
    private const val THEME_DARK = "dark"
    private const val THEME_SYSTEM = "system"

    fun applyTheme(context: Context, theme: String? = null) {
        val selectedTheme = theme ?: getSavedTheme(context)

        when (selectedTheme) {
            THEME_LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            THEME_DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            THEME_SYSTEM -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    fun saveTheme(context: Context, theme: String) {
        val prefs = context.getSharedPreferences("bookbuddy_preferences", Context.MODE_PRIVATE)
        prefs.edit()
            .putString(THEME_PREF, theme)
            .apply()
    }

    private fun getSavedTheme(context: Context): String {
        val prefs = context.getSharedPreferences("bookbuddy_preferences", Context.MODE_PRIVATE)
        return prefs.getString(THEME_PREF, THEME_SYSTEM) ?: THEME_SYSTEM
    }

    fun getCurrentTheme(context: Context): String {
        return getSavedTheme(context)
    }
}

fun Context.applySelectedTheme() {
    ThemeManager.applyTheme(this)
}