package com.example.posilkuz.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.posilkuz.data.model.Recipe
import com.example.posilkuz.data.repository.ProductRepository
import com.example.posilkuz.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipesViewModel(
    private val productRepository: ProductRepository = ProductRepository(),
    private val recipeRepository: RecipeRepository = RecipeRepository()
) : ViewModel() {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes = _recipes.asStateFlow()

    private val _userPantryIds = MutableStateFlow<Set<String>>(emptySet())
    val userPantryIds = _userPantryIds.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            // Pobieramy przepisy z nowej kolekcji "recipes" oraz ID ze spiżarni
            val recipesTask = recipeRepository.getAllRecipes() // Musisz dodać tę funkcję do repozytorium
            val pantryTask = productRepository.getUserPantryIds()

            _recipes.value = recipesTask
            _userPantryIds.value = pantryTask.toSet()
            _isLoading.value = false
        }
    }
}