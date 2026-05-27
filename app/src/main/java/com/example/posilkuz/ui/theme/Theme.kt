package com.example.posilkuz.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Wyliczenie reprezentujące możliwe tryby motywu aplikacji.
 */
enum class ThemeMode {
    /** Wymusza jasny motyw niezależnie od ustawień systemowych. */
    LIGHT,
    /** Wymusza ciemny motyw niezależnie od ustawień systemowych. */
    DARK,
    /** Stosuje motyw zgodnie z ustawieniami systemowymi urządzenia. */
    SYSTEM
}

/** Schemat kolorów dla ciemnego motywu aplikacji. */
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

/** Schemat kolorów dla jasnego motywu aplikacji. */
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

/**
 * Główny motyw aplikacji PosiłkUZ oparty na Material3.
 *
 * Wybiera schemat kolorów na podstawie [themeMode]. Obsługuje dynamiczne kolory
 * dostępne na Androidzie 12 (API 31) i nowszych, jednak domyślnie są one wyłączone,
 * aby zachować spójność z niestandardową paletą aplikacji.
 *
 * @param themeMode wybrany tryb motywu; domyślnie [ThemeMode.SYSTEM]
 * @param dynamicColor czy używać dynamicznych kolorów Material You (Android 12+);
 *   domyślnie `false`
 * @param content zawartość UI do wyrenderowania wewnątrz motywu
 */
@Composable
fun PosilkUZTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
