package com.example.posilkuz.ui.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.posilkuz.data.model.Recipe
import com.example.posilkuz.data.repository.ProductRepository
import com.example.posilkuz.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel ekranu listy przepisów kulinarnych.
 *
 * Pobiera wszystkie przepisy z [RecipeRepository] i łączy je z reaktywnym
 * strumieniem identyfikatorów produktów w spiżarni użytkownika z [ProductRepository],
 * umożliwiając oznaczanie dostępnych składników w kartach przepisów.
 *
 * @property productRepository repozytorium dostępu do danych produktów i spiżarni użytkownika
 * @property recipeRepository repozytorium dostępu do danych przepisów
 */
class RecipesViewModel(
    private val productRepository: ProductRepository = ProductRepository(),
    private val recipeRepository: RecipeRepository = RecipeRepository()
) : ViewModel() {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())

    /** Lista przepisów kulinarnych pobrana z Firebase Firestore. */
    val recipes = _recipes.asStateFlow()

    private val _isLoading = MutableStateFlow(true)

    /** Flaga informująca o trwającym pobieraniu danych z repozytorium. */
    val isLoading = _isLoading.asStateFlow()

    /**
     * Reaktywny zbiór identyfikatorów produktów aktualnie znajdujących się
     * w spiżarni zalogowanego użytkownika. Aktualizowany na bieżąco przez
     * nasłuchiwanie zmian w Firestore.
     */
    val userPantryIds = productRepository.getUserPantryIdsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    init {
        loadData()
    }

    /**
     * Pobiera listę wszystkich przepisów z repozytorium i zapisuje ją w stanie.
     * Ustawia [isLoading] na `true` podczas pobierania, a po zakończeniu na `false`.
     */
    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _recipes.value = recipeRepository.getAllRecipes()
            _isLoading.value = false
        }
    }
}
