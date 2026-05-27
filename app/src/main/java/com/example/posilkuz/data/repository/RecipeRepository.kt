package com.example.posilkuz.data.repository

import com.example.posilkuz.data.model.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repozytorium odpowiedzialne za pobieranie przepisów kulinarnych z Firebase Firestore.
 *
 * Udostępnia metody do pobierania wszystkich przepisów oraz filtrowania tych,
 * które użytkownik może przygotować na podstawie zawartości swojej spiżarni.
 *
 * @property firestore instancja Firebase Firestore używana do komunikacji z bazą danych
 */
class RecipeRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /** Referencja do kolekcji `recipes` w Firestore. */
    private val recipesCollection = firestore.collection("recipes")

    /**
     * Pobiera listę wszystkich przepisów z kolekcji `recipes` w Firestore.
     *
     * @return lista obiektów [Recipe]; pusta lista w przypadku błędu lub braku danych
     */
    suspend fun getAllRecipes(): List<Recipe> {
        return try {
            recipesCollection.get().await().toObjects(Recipe::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Pobiera przepisy, które użytkownik może wykonać na podstawie posiadanych składników.
     *
     * Przepis jest uznawany za dostępny, jeśli wszystkie jego wymagane składniki
     * ([Recipe.ingredientIds]) znajdują się na liście produktów użytkownika.
     *
     * @param userProductIds lista identyfikatorów produktów posiadanych przez użytkownika
     * @return lista obiektów [Recipe], które użytkownik może przygotować
     */
    suspend fun getAvailableRecipes(userProductIds: List<String>): List<Recipe> {
        val allRecipes = getAllRecipes()
        val userSet = userProductIds.toSet()

        return allRecipes.filter { recipe ->
            userSet.containsAll(recipe.ingredientIds)
        }
    }
}
