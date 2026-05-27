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

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    val allProducts = _allProducts.asStateFlow()

    val userPantryIds: StateFlow<Set<String>> = repository.getUserPantryIdsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    private val _unrecognizedBarcode = MutableStateFlow<String?>(null)
    val unrecognizedBarcode = _unrecognizedBarcode.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    enum class SortOrder { ASCENDING, DESCENDING }

    private val _sortOrder = MutableStateFlow(SortOrder.ASCENDING)
    val sortOrder = _sortOrder.asStateFlow()

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _allProducts.value = repository.getAllProducts()
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

    fun requestNewProduct(name: String, barcode: String) {
        viewModelScope.launch {
            repository.submitProductRequest(name, barcode)
            _unrecognizedBarcode.value = null
        }
    }

    fun closeRequestDialog() {
        _unrecognizedBarcode.value = null
    }

    suspend fun addProductByBarcode(barcode: String): AddProductResult {
        return try {
            val product = _allProducts.value.find { it.barcodes.contains(barcode) }

            if (product != null) {
                repository.addProductToPantry(product.id)
                AddProductResult.SUCCESS
            } else {
                _unrecognizedBarcode.value = barcode
                AddProductResult.NOT_FOUND
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AddProductResult.ERROR
        }
    }

    enum class AddProductResult {
        SUCCESS, NOT_FOUND, ERROR
    }

    fun toggleSortOrder() {
        _sortOrder.value = if (_sortOrder.value == SortOrder.ASCENDING) {
            SortOrder.DESCENDING
        } else {
            SortOrder.ASCENDING
        }
    }
}