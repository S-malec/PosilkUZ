package com.example.posilkuz.ui.RandomRecipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.posilkuz.data.model.Recipe
import com.example.posilkuz.data.repository.ProductRepository
import com.example.posilkuz.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel ekranu losowego przepisu, obsługujący fizykę animacji i logikę losowania.
 *
 * Odczytuje dane z akcelerometru (przekazywane z warstwy UI) i symuluje ruch
 * emoji jabłka po ekranie z odbiciami od krawędzi. Potrząśnięcie telefonem
 * wyzwala losowanie przepisu z [RecipeRepository]. Udostępnia również
 * identyfikatory produktów w spiżarni użytkownika do oznaczania składników.
 *
 * @property recipieRepository repozytorium przepisów używane do losowania
 * @property productRepository repozytorium produktów używane do pobierania zawartości spiżarni
 */
class RandomRecipeViewModel(
    private val recipieRepository: RecipeRepository = RecipeRepository(),
    private val productRepository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _recipe = MutableStateFlow<Recipe?>(null)

    /** Aktualnie wylosowany przepis lub `null`, jeśli żaden nie został jeszcze wylosowany. */
    val recipe: StateFlow<Recipe?> = _recipe.asStateFlow()

    private val _isLoading = MutableStateFlow(false)

    /** Flaga informująca o trwającym pobieraniu przepisu z repozytorium. */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _offsetX = MutableStateFlow(0f)

    /** Poziome przesunięcie emoji jabłka w pikselach względem środka ekranu. */
    val offsetX: StateFlow<Float> = _offsetX.asStateFlow()

    private val _offsetY = MutableStateFlow(0f)

    /** Pionowe przesunięcie emoji jabłka w pikselach względem środka ekranu. */
    val offsetY: StateFlow<Float> = _offsetY.asStateFlow()

    /** Aktualna prędkość pozioma emoji jabłka (px/klatkę). */
    var velocityX = 0f

    /** Aktualna prędkość pionowa emoji jabłka (px/klatkę). */
    var velocityY = 0f

    /** Aktualne przyspieszenie poziome odczytane z akcelerometru (m/s²). */
    var accelX = 0f

    /** Aktualne przyspieszenie pionowe odczytane z akcelerometru (m/s²). */
    var accelY = 0f

    /**
     * Reaktywny zbiór identyfikatorów produktów aktualnie w spiżarni zalogowanego użytkownika.
     * Aktualizowany na bieżąco przez nasłuchiwanie zmian w Firestore.
     */
    val userPantryIds = productRepository.getUserPantryIdsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    /**
     * Aktualizuje wartości przyspieszenia używane w symulacji fizyki.
     *
     * Ignoruje wywołania, gdy przepis jest już wylosowany lub trwa ładowanie.
     *
     * @param x przyspieszenie wzdłuż osi X (m/s²)
     * @param y przyspieszenie wzdłuż osi Y (m/s²)
     */
    fun onAcceleration(x: Float, y: Float) {
        if (_recipe.value != null || _isLoading.value) return
        accelX = x
        accelY = y
    }

    /**
     * Oblicza nową pozycję emoji jabłka na podstawie aktualnego przyspieszenia i prędkości.
     *
     * Stosuje tłumienie prędkości (współczynnik 0,9) oraz odbicie od krawędzi ekranu
     * ze stratą energii (współczynnik 0,7). Wywołanie z każdą klatką animacji (~16 ms).
     * Ignoruje wywołania, gdy przepis jest już wylosowany lub trwa ładowanie.
     *
     * @param maxX maksymalne przesunięcie poziome w pikselach (połowa szerokości obszaru)
     * @param maxY maksymalne przesunięcie pionowe w pikselach (połowa wysokości obszaru)
     */
    fun updatePhysics(maxX: Float, maxY: Float) {
        if (_recipe.value != null || _isLoading.value) return

        velocityX += accelX * 2f
        velocityY -= accelY * 2f

        velocityX *= 0.9f
        velocityY *= 0.9f

        var newX = _offsetX.value + velocityX
        var newY = _offsetY.value + velocityY

        if (newX > maxX) { newX = maxX; velocityX = -velocityX * 0.7f }
        if (newX < -maxX) { newX = -maxX; velocityX = -velocityX * 0.7f }
        if (newY > maxY) { newY = maxY; velocityY = -velocityY * 0.7f }
        if (newY < -maxY) { newY = -maxY; velocityY = -velocityY * 0.7f }

        _offsetX.value = newX
        _offsetY.value = newY
    }

    /**
     * Losuje przepis z repozytorium po wykryciu potrząśnięcia telefonem.
     *
     * Ignoruje wywołanie, gdy trwa ładowanie lub przepis jest już wylosowany.
     * Po zakończeniu ustawia [recipe] na losowy przepis lub `null`, jeśli lista jest pusta.
     */
    fun onShake() {
        if (_isLoading.value || _recipe.value != null) return
        viewModelScope.launch {
            _isLoading.value = true
            val all = recipieRepository.getAllRecipes()
            _recipe.value = all.randomOrNull()
            _isLoading.value = false
        }
    }

    /**
     * Odrzuca aktualnie wylosowany przepis i przywraca stan animacji jabłka.
     */
    fun dismissRecipe() {
        _recipe.value = null
    }
}
