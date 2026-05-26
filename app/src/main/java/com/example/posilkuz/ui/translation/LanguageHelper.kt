package com.example.posilkuz.ui.translation

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale
import androidx.core.content.edit

object LanguageHelper {
    fun setLocale(context: Context, languageCode: String): Context {
        val locale = if (languageCode == "system")
            Resources.getSystem().configuration.locales[0]
        else
            Locale(languageCode)

        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getString("language", "system") ?: "system"
    }

    fun saveLanguage(context: Context, languageCode: String) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit {
                putString("language", languageCode)
            }
    }
}