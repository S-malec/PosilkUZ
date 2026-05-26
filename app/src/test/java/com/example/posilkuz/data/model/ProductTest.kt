package com.example.posilkuz.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductTest {

    @Test
    fun `default constructor should create empty product`() {
        // Ważne dla Firebase Firestore - musi istnieć pusty konstruktor
        val product = Product()

        assertEquals("", product.id)
        assertEquals("", product.name)
        assertEquals("", product.category)
        assertEquals("", product.unit)
        assertTrue(product.barcodes.isEmpty())
    }

    @Test
    fun `product initialization with values should set properties correctly`() {
        val product = Product(
            id = "test_id",
            name = "Mleko",
            category = "Nabiał",
            unit = "l",
            barcodes = listOf("1234567890")
        )

        assertEquals("test_id", product.id)
        assertEquals("Mleko", product.name)
        assertEquals("Nabiał", product.category)
        assertEquals("l", product.unit)
        assertEquals(1, product.barcodes.size)
        assertEquals("1234567890", product.barcodes.first())
    }
}
