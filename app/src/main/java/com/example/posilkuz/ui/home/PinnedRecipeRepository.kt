package com.example.posilkuz.data.repository

import com.example.posilkuz.data.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton przechowujący przypięty przepis w pamięci przez całą sesję.
 * Dostępny z dowolnego miejsca bez przekazywania przez composable.
 */
object PinnedRecipeRepository {
    private val _pinnedRecipe = MutableStateFlow<Recipe?>(null)
    val pinnedRecipe: StateFlow<Recipe?> = _pinnedRecipe.asStateFlow()

    fun pin(recipe: Recipe) {
        _pinnedRecipe.value = recipe
    }

    fun unpin() {
        _pinnedRecipe.value = null
    }

    fun toggle(recipe: Recipe) {
        if (_pinnedRecipe.value?.title == recipe.title) unpin() else pin(recipe)
    }

    fun isPinned(recipe: Recipe): Boolean = _pinnedRecipe.value?.title == recipe.title
}