package com.example.posilkuz.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeTest {

    @Test
    fun `default constructor should create empty recipe`() {
        // Ważne dla Firebase Firestore
        val recipe = Recipe()

        assertEquals("", recipe.id)
        assertEquals("", recipe.title)
        assertEquals("", recipe.description)
        assertEquals("", recipe.instructions)
        assertTrue(recipe.ingredientIds.isEmpty())
        assertTrue(recipe.ingredientsAmount.isEmpty())
    }

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
