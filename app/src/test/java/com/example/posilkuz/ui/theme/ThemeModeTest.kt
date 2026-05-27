package com.example.posilkuz.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Testy jednostkowe wyliczenia [ThemeMode].
 *
 * Weryfikują, czy wyliczenie zawiera dokładnie oczekiwane wartości
 * i czy można je poprawnie odczytać po nazwie.
 */
class ThemeModeTest {

    /**
     * Sprawdza, czy [ThemeMode] zawiera dokładnie trzy wartości: [ThemeMode.LIGHT],
     * [ThemeMode.DARK] i [ThemeMode.SYSTEM], oraz czy można je odczytać przez [ThemeMode.valueOf].
     */
    @Test
    fun `ThemeMode should contain LIGHT, DARK, SYSTEM`() {
        val modes = ThemeMode.values()

        assertEquals(3, modes.size)
        assertEquals(ThemeMode.LIGHT, ThemeMode.valueOf("LIGHT"))
        assertEquals(ThemeMode.DARK, ThemeMode.valueOf("DARK"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.valueOf("SYSTEM"))
    }
}
