package com.example.posilkuz.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testy jednostkowe klasy danych [Recipe].
 *
 * Weryfikują poprawność domyślnych wartości konstruktora (wymaganych przez Firestore)
 * oraz poprawność inicjalizacji obiektu ze składnikami i ich ilościami.
 */
class RecipeTest {

    /**
     * Sprawdza, czy konstruktor bezparametrowy tworzy obiekt z pustymi wartościami domyślnymi.
     *
     * Pusty konstruktor jest wymagany przez Firebase Firestore do automatycznej
     * deserializacji dokumentów przy użyciu refleksji.
     */
    @Test
    fun `default constructor should create empty recipe`() {
        val recipe = Recipe()

        assertEquals("", recipe.id)
        assertEquals("", recipe.title)
        assertEquals("", recipe.description)
        assertEquals("", recipe.instructions)
        assertTrue(recipe.ingredientIds.isEmpty())
        assertTrue(recipe.ingredientsAmount.isEmpty())
    }

    /**
     * Sprawdza, czy lista składników ([Recipe.ingredientIds]) i mapa ilości
     * ([Recipe.ingredientsAmount]) są poprawnie przechowywane po inicjalizacji obiektu.
     */
    @Test
    fun `recipe initialization should correctly store ingredients`() {
        val recipe = Recipe(
            title = "Jajecznica",
            ingredientIds = listOf("jajko", "maslo"),
            ingredientsAmount = mapOf("jajko" to "3 szt.", "maslo" to "10g")
        )

        assertEquals("Jajecznica", recipe.title)
        assertEquals(2, recipe.ingredientIds.size)
        assertTrue(recipe.ingredientIds.contains("jajko"))
        assertEquals("3 szt.", recipe.ingredientsAmount["jajko"])
    }
}
