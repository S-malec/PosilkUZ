package com.example.posilkuz.ui.pantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.posilkuz.data.model.Product
import com.example.posilkuz.data.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PantryViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    // 1. Lista produktów - może zostać jako StateFlow (pobierana raz lub rzadko)
    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    val allProducts = _allProducts.asStateFlow()

    // 2. KLUCZOWA ZMIANA: userPantryIds teraz słucha Flow z repozytorium
    val userPantryIds: StateFlow<Set<String>> = repository.getUserPantryIdsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val groupedProducts = allProducts
        .map { products -> products.groupBy { it.category } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            // Pobieramy listę wszystkich dostępnych produktów (np. z JSONa/Firestore)
            _allProducts.value = repository.getAllProducts()
            // Nie musimy już ręcznie pobierać userPantryIds, bo Flow powyżej sam to robi
            _isLoading.value = false
        }
    }

    fun toggleProduct(productId: String) {
        viewModelScope.launch {
            val currentPantry = userPantryIds.value
            if (currentPantry.contains(productId)) {
                repository.removeProductFromPantry(productId)
            } else {
                repository.addProductToPantry(productId)
            }
        }
    }

    fun addProductByBarcode(barcode: String) {
        viewModelScope.launch {
            val product = _allProducts.value.find { it.barcode == barcode }

            if (product != null) {
                repository.addProductToPantry(product.id)
            } else {
                // POZNIEJ DODAJ TU DO LISTY PRODUKTOW DO ZATWIERDZENIA I WYSWIETL Z TYM ZWIAZANY KOMUNIKAT
            }
        }
    }
}