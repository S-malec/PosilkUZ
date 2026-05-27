package com.example.posilkuz.ui.translation

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale
import androidx.core.content.edit

/**
 * Pomocnik do zarządzania językiem (lokalizacją) aplikacji w czasie wykonania.
 *
 * Umożliwia programową zmianę języka bez restartu procesu — wystarczy ponownie
 * uruchomić aktywność ([android.app.Activity.recreate]). Wybrany język jest
 * trwale zapisywany w SharedPreferences pod kluczem `"language"`.
 */
object LanguageHelper {

    /**
     * Tworzy nowy kontekst z ustawioną lokalizacją odpowiadającą podanemu kodowi języka.
     *
     * Jeśli kod to `"system"`, stosowana jest lokalizacja systemowa urządzenia.
     *
     * @param context bazowy kontekst aplikacji lub aktywności
     * @param languageCode kod języka BCP 47, np. `"pl"`, `"en"`, lub `"system"`
     * @return nowy kontekst z ustawioną lokalizacją
     */
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

    /**
     * Odczytuje zapisany kod języka z SharedPreferences.
     *
     * @param context kontekst aplikacji potrzebny do dostępu do SharedPreferences
     * @return zapisany kod języka lub `"system"`, jeśli nie dokonano żadnego wyboru
     */
    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getString("language", "system") ?: "system"
    }

    /**
     * Zapisuje wybrany kod języka w SharedPreferences.
     *
     * @param context kontekst aplikacji potrzebny do dostępu do SharedPreferences
     * @param languageCode kod języka BCP 47 do zapisania, np. `"pl"`, `"en"`, lub `"system"`
     */
    fun saveLanguage(context: Context, languageCode: String) {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit {
                putString("language", languageCode)
            }
    }
}
