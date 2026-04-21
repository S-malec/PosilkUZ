package com.example.posilkuz.ui.pantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.posilkuz.data.model.Product
import com.example.posilkuz.data.normalizePolish
import com.example.posilkuz.data.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PantryViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    val allProducts = _allProducts.asStateFlow()

    val userPantryIds: StateFlow<Set<String>> = repository.getUserPantryIdsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    enum class SortOrder { ASCENDING, DESCENDING }

    // W PantryViewModel.kt
    private val _sortOrder = MutableStateFlow(SortOrder.ASCENDING)
    val sortOrder = _sortOrder.asStateFlow()
    val groupedProducts = combine(allProducts, _searchQuery, _sortOrder) { products, query, order ->
        val normalizedQuery = query.normalizePolish()

        val filtered = products.filter {
            it.name.normalizePolish().contains(normalizedQuery)
        }

        val sorted = when (order) {
            SortOrder.ASCENDING -> filtered.sortedBy { it.name.normalizePolish() }
            SortOrder.DESCENDING -> filtered.sortedByDescending { it.name.normalizePolish() }
        }

        sorted.groupBy { it.category }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

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

    fun toggleSortOrder() {
        _sortOrder.value = if (_sortOrder.value == SortOrder.ASCENDING) {
            SortOrder.DESCENDING
        } else {
            SortOrder.ASCENDING
        }
    }
}