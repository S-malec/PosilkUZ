package com.example.posilkuz.data

import org.junit.Assert.assertEquals
import org.junit.Test

class StringUtilsTest {

    @Test
    fun `normalizePolish should remove polish diacritics and convert to lowercase`() {
        val input = "ZaZółć Gęślą Jaźń"
        val expected = "zazolc gesla jazn"
        
        val result = input.normalizePolish()
        
        assertEquals(expected, result)
    }

    @Test
    fun `normalizePolish should handle all polish specific characters correctly`() {
        val input = "ĄĆĘŁŃÓŚŹŻ ąćęłńóśźż"
        val expected = "acelnoszz acelnoszz"
        
        val result = input.normalizePolish()
        
        assertEquals(expected, result)
    }

    @Test
    fun `normalizePolish should not modify strings without polish diacritics`() {
        val input = "Hello World 123!"
        val expected = "hello world 123!" // Zwróć uwagę, że funkcja również zamienia na małe litery
        
        val result = input.normalizePolish()
        
        assertEquals(expected, result)
    }

    @Test
    fun `normalizePolish should handle empty string`() {
        val input = ""
        val expected = ""
        
        val result = input.normalizePolish()
        
        assertEquals(expected, result)
    }
}
