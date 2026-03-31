package com.example.posilkuz.data.repository

import com.example.posilkuz.data.model.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class RecipeRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val recipesCollection = firestore.collection("recipes")

    // Pobiera wszystkie przepisy z bazy
    suspend fun getAllRecipes(): List<Recipe> {
        return try {
            recipesCollection.get().await().toObjects(Recipe::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Główna logika: Pobiera przepisy, które użytkownik MOŻE zrobić
     * @param userProductIds lista ID składników, które ma użytkownik
     */
    suspend fun getAvailableRecipes(userProductIds: List<String>): List<Recipe> {
        val allRecipes = getAllRecipes()
        val userSet = userProductIds.toSet()

        return allRecipes.filter { recipe ->
            // Przepis jest dostępny, jeśli wszystkie jego ingredientIds są w posiadaniu użytkownika
            userSet.containsAll(recipe.ingredientIds)
        }
    }
}