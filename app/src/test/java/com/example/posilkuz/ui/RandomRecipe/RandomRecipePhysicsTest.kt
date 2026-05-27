package com.example.posilkuz.ui.RandomRecipe

import com.example.posilkuz.data.repository.ProductRepository
import com.example.posilkuz.data.repository.RecipeRepository
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Testy jednostkowe silnika fizyki w [RandomRecipeViewModel].
 *
 * Weryfikują poprawność symulacji ruchu emoji jabłka: aktualizację pozycji
 * na podstawie przyspieszenia akcelerometru oraz mechanizm odbicia od krawędzi ekranu.
 * Repozytoria są zastąpione mockami, aby uniknąć wywołań Firebase podczas testów.
 */
class RandomRecipePhysicsTest {

    private lateinit var viewModel: RandomRecipeViewModel

    /**
     * Inicjalizuje [RandomRecipeViewModel] z mockami repozytoriów przed każdym testem.
     *
     * Mockuje [RecipeRepository] i [ProductRepository] w trybie `relaxed`,
     * a dla [ProductRepository.getUserPantryIdsFlow] zwraca pusty [MutableStateFlow],
     * aby ViewModel mógł się zainicjalizować bez połączenia z Firebase Firestore.
     */
    @Before
    fun setup() {
        val mockRecipeRepo = mockk<RecipeRepository>(relaxed = true)
        val mockProductRepo = mockk<ProductRepository>(relaxed = true) {
            io.mockk.every { getUserPantryIdsFlow() } returns MutableStateFlow(emptySet())
        }

        viewModel = RandomRecipeViewModel(mockRecipeRepo, mockProductRepo)
    }

    /**
     * Sprawdza, czy po jednej klatce fizyki pozycja emoji jest poprawnie obliczona
     * na podstawie przyspieszenia w osi X.
     *
     * Przy przyspieszeniu x=5, y=0 oczekiwana prędkość po tłumieniu wynosi 9 px/klatkę,
     * co przekłada się na przesunięcie offsetX = 9 i brak zmiany offsetY.
     */
    @Test
    fun `updatePhysics should update offset based on acceleration`() {
        viewModel.onAcceleration(5f, 0f)

        viewModel.updatePhysics(maxX = 100f, maxY = 100f)

        assertEquals(9f, viewModel.offsetX.value, 0.01f)
        assertEquals(0f, viewModel.offsetY.value, 0.01f)
    }

    /**
     * Sprawdza, czy emoji odbija się od krawędzi ekranu i zmienia kierunek ruchu.
     *
     * Przy ekstremalnym przyspieszeniu (x=100) prędkość przekracza maxX=50,
     * więc pozycja powinna zostać obcięta do maxX, a prędkość powinna zmienić
     * znak (odbicie z tłumieniem 0.7).
     */
    @Test
    fun `updatePhysics should bounce off the walls`() {
        viewModel.onAcceleration(100f, 0f)

        viewModel.updatePhysics(maxX = 50f, maxY = 50f)

        assertEquals(50f, viewModel.offsetX.value, 0.01f)

        assertTrue("Prędkość powinna być ujemna (odbicie)", viewModel.velocityX < 0f)
    }
}
