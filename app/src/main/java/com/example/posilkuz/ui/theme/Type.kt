package com.example.posilkuz.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Definicja typografii aplikacji oparta na Material3 [Typography].
 *
 * Nadpisuje styl `bodyLarge` ustawiając domyślną czcionkę systemową,
 * normalną grubość, rozmiar 16sp i interlignię 24sp.
 * Pozostałe style typograficzne korzystają z wartości domyślnych Material3.
 */
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)
