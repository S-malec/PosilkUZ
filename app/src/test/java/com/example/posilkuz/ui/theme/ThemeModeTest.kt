package com.example.posilkuz.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `ThemeMode should contain LIGHT, DARK, SYSTEM`() {
        val modes = ThemeMode.values()
        
        assertEquals(3, modes.size)
        assertEquals(ThemeMode.LIGHT, ThemeMode.valueOf("LIGHT"))
        assertEquals(ThemeMode.DARK, ThemeMode.valueOf("DARK"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.valueOf("SYSTEM"))
    }
}
