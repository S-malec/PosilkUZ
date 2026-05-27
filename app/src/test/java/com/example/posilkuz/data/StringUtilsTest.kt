package com.example.posilkuz.data

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Testy jednostkowe funkcji rozszerzającej [String.normalizePolish].
 *
 * Weryfikują poprawność zamiany polskich liter diakrytycznych na ich łacińskie
 * odpowiedniki oraz konwersji do małych liter.
 */
class StringUtilsTest {

    /**
     * Sprawdza, czy funkcja poprawnie usuwa polskie znaki diakrytyczne
     * i zamienia wynik na małe litery dla typowego zdania z wieloma diakrytykami.
     */
    @Test
    fun `normalizePolish should remove polish diacritics and convert to lowercase`() {
        val input = "ZaZółć Gęślą Jaźń"
        val expected = "zazolc gesla jazn"

        val result = input.normalizePolish()

        assertEquals(expected, result)
    }

    /**
     * Sprawdza, czy wszystkie polskie znaki diakrytyczne (zarówno małe, jak i wielkie)
     * są poprawnie zastępowane odpowiednikami łacińskimi.
     */
    @Test
    fun `normalizePolish should handle all polish specific characters correctly`() {
        val input = "ĄĆĘŁŃÓŚŹŻ ąćęłńóśźż"
        val expected = "acelnoszz acelnoszz"

        val result = input.normalizePolish()

        assertEquals(expected, result)
    }

    /**
     * Sprawdza, czy ciąg bez polskich znaków diakrytycznych jest jedynie zamieniany
     * na małe litery, bez innych modyfikacji.
     */
    @Test
    fun `normalizePolish should not modify strings without polish diacritics`() {
        val input = "Hello World 123!"
        val expected = "hello world 123!"

        val result = input.normalizePolish()

        assertEquals(expected, result)
    }

    /**
     * Sprawdza, czy funkcja poprawnie obsługuje pusty ciąg znaków,
     * zwracając pusty ciąg bez rzucenia wyjątku.
     */
    @Test
    fun `normalizePolish should handle empty string`() {
        val input = ""
        val expected = ""

        val result = input.normalizePolish()

        assertEquals(expected, result)
    }
}
