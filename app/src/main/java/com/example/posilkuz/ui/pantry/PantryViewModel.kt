package com.example.posilkuz.ui.pantry

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import androidx.lifecycle.viewModelScope
import com.example.posilkuz.data.model.Product
import com.example.posilkuz.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PantryViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    // Lista wszystkich produktów dostępnych w bazie
    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    val allProducts = _allProducts.asStateFlow()

    // Zbiór ID produktów, które użytkownik ma w spiżarni
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
            // Pobieramy dane równolegle
            val productsDeferred = async { repository.getAllProducts() }
            val pantryDeferred = async { repository.getUserPantryIds() }

            _allProducts.value = productsDeferred.await()
            _userPantryIds.value = pantryDeferred.await().toSet()
            _isLoading.value = false
        }
    }

    fun toggleProduct(productId: String) {
        viewModelScope.launch {
            val currentPantry = _userPantryIds.value
            if (currentPantry.contains(productId)) {
                // Usuwamy lokalnie (szybka reakcja UI) i w bazie
                _userPantryIds.value = currentPantry - productId
                repository.removeProductFromPantry(productId)
            } else {
                // Dodajemy lokalnie i w bazie
                _userPantryIds.value = currentPantry + productId
                repository.addProductToPantry(productId)
            }
        }
    }
}