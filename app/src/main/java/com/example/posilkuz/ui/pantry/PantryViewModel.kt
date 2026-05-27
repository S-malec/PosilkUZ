package com.example.posilkuz.ui.pantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.posilkuz.data.model.Product
import com.example.posilkuz.data.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel ekranu spiżarni zarządzający stanem listy produktów i spiżarni użytkownika.
 *
 * Pobiera wszystkie dostępne produkty z [ProductRepository] i łączy je z reaktywnym
 * strumieniem identyfikatorów produktów w spiżarni użytkownika. Udostępnia przefiltrowaną
 * i posortowaną listę produktów pogrupowanych według kategorii.
 *
 * @property repository repozytorium dostępu do danych produktów
 */
class PantryViewModel(
    private val repository: ProductRepository = ProductRepository()
) : ViewModel() {

    /** Pełna lista wszystkich produktów dostępnych w systemie. */
    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())
    val allProducts = _allProducts.asStateFlow()

    /**
     * Reaktywny zbiór identyfikatorów produktów aktualnie znajdujących się w spiżarni zalogowanego użytkownika.
     * Aktualizowany na bieżąco przez nasłuchiwanie zmian w Firestore.
     */
    val userPantryIds: StateFlow<Set<String>> = repository.getUserPantryIdsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    /** Kod kreskowy nierozpoznanego produktu oczekującego na zgłoszenie, lub `null` gdy dialog jest zamknięty. */
    private val _unrecognizedBarcode = MutableStateFlow<String?>(null)
    val unrecognizedBarcode = _unrecognizedBarcode.asStateFlow()

    /** Flaga informująca o trwającym pobieraniu danych z repozytorium. */
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    /** Aktualna fraza wyszukiwania wpisana przez użytkownika. */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    /** Wyliczenie określające kierunek sortowania listy produktów. */
    enum class SortOrder { ASCENDING, DESCENDING }

    private val _sortOrder = MutableStateFlow(SortOrder.ASCENDING)

    /** Aktualny porządek sortowania produktów. */
    val sortOrder = _sortOrder.asStateFlow()

    /**
     * Aktualizuje frazę wyszukiwania produktów.
     *
     * @param newQuery nowe zapytanie wpisane przez użytkownika
     */
    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    init {
        loadData()
    }

    /**
     * Pobiera listę wszystkich dostępnych produktów z repozytorium i zapisuje ją w stanie.
     * Ustawia [isLoading] na `true` podczas pobierania, a po zakończeniu na `false`.
     */
    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _allProducts.value = repository.getAllProducts()
            _isLoading.value = false
        }
    }

    /**
     * Przełącza obecność produktu w spiżarni użytkownika.
     *
     * Jeśli produkt jest już w spiżarni, zostaje z niej usunięty; w przeciwnym razie zostaje dodany.
     *
     * @param productId identyfikator produktu do przełączenia
     */
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

    /**
     * Wysyła zgłoszenie nowego produktu do bazy danych i zamyka dialog zgłoszenia.
     *
     * @param name proponowana nazwa produktu
     * @param barcode kod kreskowy zgłaszanego produktu
     */
    fun requestNewProduct(name: String, barcode: String) {
        viewModelScope.launch {
            repository.submitProductRequest(name, barcode)
            _unrecognizedBarcode.value = null
        }
    }

    /**
     * Zamknięcia dialog zgłoszenia nowego produktu bez wysyłania zgłoszenia.
     */
    fun closeRequestDialog() {
        _unrecognizedBarcode.value = null
    }

    /**
     * Próbuje dodać produkt do spiżarni na podstawie zeskanowanego kodu kreskowego.
     *
     * Szuka produktu na lokalnej liście po kodzie kreskowym. Jeśli produkt zostanie
     * znaleziony, dodaje go do spiżarni. Jeśli nie — ustawia [unrecognizedBarcode],
     * co powoduje wyświetlenie dialogu zgłoszenia nowego produktu.
     *
     * @param barcode zeskanowany kod kreskowy produktu
     * @return wynik operacji: [AddProductResult.SUCCESS], [AddProductResult.NOT_FOUND]
     *   lub [AddProductResult.ERROR]
     */
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

    /**
     * Wyliczenie reprezentujące możliwe wyniki próby dodania produktu przez kod kreskowy.
     */
    enum class AddProductResult {
        /** Produkt znaleziony i dodany do spiżarni pomyślnie. */
        SUCCESS,
        /** Produkt o podanym kodzie kreskowym nie istnieje w bazie danych. */
        NOT_FOUND,
        /** Wystąpił nieoczekiwany błąd podczas operacji. */
        ERROR
    }

    /**
     * Przełącza porządek sortowania produktów między rosnącym a malejącym.
     */
    fun toggleSortOrder() {
        _sortOrder.value = if (_sortOrder.value == SortOrder.ASCENDING) {
            SortOrder.DESCENDING
        } else {
            SortOrder.ASCENDING
        }
    }
}
