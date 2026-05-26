package com.example.posilkuz.ui.RandomRecipe

import com.example.posilkuz.data.repository.ProductRepository
import com.example.posilkuz.data.repository.RecipeRepository
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RandomRecipePhysicsTest {

    private lateinit var viewModel: RandomRecipeViewModel

    @Before
    fun setup() {
        // Tworzymy "zaślepki" (Mocki) dla repozytoriów, aby ViewModel przy tworzeniu
        // NIE próbował łączyć się z Firebase Firestore (co kończyłoby się błędem myPid not mocked)
        val mockRecipeRepo = mockk<RecipeRepository>(relaxed = true)
        val mockProductRepo = mockk<ProductRepository>(relaxed = true) {
            // Ponieważ ViewModel w init (lub polach) wymaga tego Flow:
            io.mockk.every { getUserPantryIdsFlow() } returns MutableStateFlow(emptySet())
        }

        // Inicjalizujemy ViewModel z naszymi "oszukanymi" repozytoriami
        viewModel = RandomRecipeViewModel(mockRecipeRepo, mockProductRepo)
    }

    @Test
    fun `updatePhysics should update offset based on acceleration`() {
        // Symulacja wychylenia telefonu w prawo (akcelerometr X > 0)
        viewModel.onAcceleration(5f, 0f)
        
        // Jedna klatka fizyki
        viewModel.updatePhysics(maxX = 100f, maxY = 100f)
        
        // Zgodnie z kodem: velocityX = (0 + 5*2) * 0.9 = 9
        // offsetX = 0 + 9 = 9
        assertEquals(9f, viewModel.offsetX.value, 0.01f)
        assertEquals(0f, viewModel.offsetY.value, 0.01f)
    }

    @Test
    fun `updatePhysics should bounce off the walls`() {
        // Ekstremalne przyspieszenie, żeby uderzyć w krawędź
        viewModel.onAcceleration(100f, 0f)
        
        // Pętla fizyki
        viewModel.updatePhysics(maxX = 50f, maxY = 50f)
        
        // Ponieważ max to 50, a prędkość wyniesie 180, pozycja powinna zostać obcięta do maxX (50)
        assertEquals(50f, viewModel.offsetX.value, 0.01f)
        
        // Dodatkowo, prędkość powinna się odwrócić (odbicie z tłumieniem 0.7)
        // Oczekiwana prędkość: - (180 * 0.9) * 0.7 = -113.4
        assertTrue("Prędkość powinna być ujemna (odbicie)", viewModel.velocityX < 0f)
    }
}
