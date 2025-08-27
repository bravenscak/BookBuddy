package com.bruno.bookbuddy.utils

import android.content.Context
import android.content.res.Configuration
import java.util.*

object LocaleHelper {

    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    fun getCurrentLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("bookbuddy_preferences", Context.MODE_PRIVATE)

        val savedLanguage = prefs.getString("language", null)

        return savedLanguage ?: when {
            Locale.getDefault().language == "hr" -> "hr"
            else -> "en"
        }
    }

    fun saveLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences("bookbuddy_preferences", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("language", language)
            .apply()
    }
}