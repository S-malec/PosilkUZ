package com.example.posilkuz.ui.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.example.posilkuz.R
import com.example.posilkuz.ui.translation.TranslationHelper

/**
 * Otwiera aplikację Google Maps z wynikami wyszukiwania sklepów spożywczych w pobliżu.
 *
 * Próbuje uruchomić natywną aplikację Google Maps z intencją `geo:`. Jeśli aplikacja
 * nie jest zainstalowana, otwiera wyniki wyszukiwania w przeglądarce internetowej
 * pod adresem `maps.google.com`.
 *
 * @param query fraza wyszukiwania przekazywana do map; domyślnie lokalizowana nazwa
 *   sklepów spożywczych z zasobów tekstowych aplikacji
 */
fun Context.openGroceryMaps(query: String = getString(R.string.grocery_stores)) {
    val uri = "geo:0,0?q=${Uri.encode(query)}".toUri()
    val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }

    try {
        startActivity(mapIntent)
    } catch (_: Exception) {
        val webIntent = Intent(Intent.ACTION_VIEW,
            "https://www.google.com/maps/search/${Uri.encode(query)}".toUri())
        startActivity(webIntent)
    }
}
