package com.example.posilkuz.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testy jednostkowe klasy danych [Product].
 *
 * Weryfikują poprawność domyślnych wartości konstruktora (wymaganych przez Firestore)
 * oraz poprawność inicjalizacji obiektu z podanymi wartościami.
 */
class ProductTest {

    /**
     * Sprawdza, czy konstruktor bezparametrowy tworzy obiekt z pustymi wartościami domyślnymi.
     *
     * Pusty konstruktor jest wymagany przez Firebase Firestore do automatycznej
     * deserializacji dokumentów przy użyciu refleksji.
     */
    @Test
    fun `default constructor should create empty product`() {
        val product = Product()

        assertEquals("", product.id)
        assertEquals("", product.name)
        assertEquals("", product.category)
        assertEquals("", product.unit)
        assertTrue(product.barcodes.isEmpty())
    }

    /**
     * Sprawdza, czy wszystkie właściwości obiektu [Product] są poprawnie ustawiane
     * podczas inicjalizacji z explicite podanymi wartościami.
     */
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
