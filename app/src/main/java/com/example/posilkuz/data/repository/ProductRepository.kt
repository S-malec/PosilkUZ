package com.example.posilkuz.data.repository

import com.example.posilkuz.data.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repozytorium odpowiedzialne za zarządzanie produktami i spiżarnią użytkownika.
 *
 * Komunikuje się z Firebase Firestore w celu pobierania globalnej listy produktów,
 * odczytu i modyfikacji spiżarni zalogowanego użytkownika oraz obsługi zgłoszeń
 * nowych produktów przez użytkowników i administratorów.
 *
 * @property firestore instancja Firebase Firestore używana do operacji na bazie danych
 * @property auth instancja Firebase Authentication używana do identyfikacji zalogowanego użytkownika
 */
class ProductRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    /** Referencja do kolekcji `products` w Firestore. */
    private val productsCollection = firestore.collection("products")

    /**
     * Pobiera listę wszystkich dostępnych produktów z kolekcji `products` w Firestore.
     *
     * @return lista obiektów [Product] z uzupełnionym polem [Product.id];
     *   pusta lista w przypadku błędu połączenia lub braku danych
     */
    suspend fun getAllProducts(): List<Product> {
        return try {
            val snapshot = productsCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Pobiera jednorazowo identyfikatory produktów znajdujących się w spiżarni zalogowanego użytkownika.
     *
     * @return lista identyfikatorów produktów (pole `pantry` w dokumencie użytkownika);
     *   pusta lista, jeśli użytkownik nie jest zalogowany lub wystąpił błąd
     */
    suspend fun getUserPantryIds(): List<String> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            @Suppress("UNCHECKED_CAST")
            document.get("pantry") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Zwraca reaktywny strumień [Flow] identyfikatorów produktów w spiżarni zalogowanego użytkownika.
     *
     * Nasłuchuje zmian dokumentu użytkownika w Firestore w czasie rzeczywistym.
     * Emituje nową wartość za każdym razem, gdy zawartość spiżarni zostanie zmieniona.
     * W przypadku braku zalogowanego użytkownika natychmiast emituje pusty zbiór.
     *
     * @return [Flow] emitujący aktualny zbiór identyfikatorów produktów w spiżarni
     */
    fun getUserPantryIdsFlow(): Flow<Set<String>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptySet())
            return@callbackFlow
        }

        val listener = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Błąd Firestore: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val pantryList = snapshot.get("pantry") as? List<String> ?: emptyList()
                    trySend(pantryList.toSet())
                } else {
                    trySend(emptySet())
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Nadpisuje całą zawartość spiżarni zalogowanego użytkownika podaną listą produktów.
     *
     * @param newPantry nowa lista identyfikatorów produktów, która zastąpi dotychczasową spiżarnię
     */
    suspend fun updatePantry(newPantry: List<String>) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .update("pantry", newPantry).await()
    }

    /**
     * Dodaje pojedynczy produkt do spiżarni zalogowanego użytkownika.
     *
     * Operacja jest atomowa — używa [FieldValue.arrayUnion], więc nie duplikuje produktu,
     * jeśli już istnieje w spiżarni.
     *
     * @param productId identyfikator produktu do dodania
     */
    suspend fun addProductToPantry(productId: String) {
        val userId = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(userId)
                .update("pantry", FieldValue.arrayUnion(productId))
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Usuwa pojedynczy produkt ze spiżarni zalogowanego użytkownika.
     *
     * Operacja jest atomowa — używa [FieldValue.arrayRemove].
     *
     * @param productId identyfikator produktu do usunięcia
     */
    suspend fun removeProductFromPantry(productId: String) {
        val userId = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(userId)
                .update("pantry", FieldValue.arrayRemove(productId))
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Wysyła zgłoszenie nowego produktu do kolekcji `product_requests` w Firestore.
     *
     * Zgłoszenie zawiera nazwę produktu, kod kreskowy, identyfikator zgłaszającego użytkownika,
     * status `"pending"` oraz znacznik czasu serwera.
     *
     * @param name proponowana nazwa nowego produktu
     * @param barcode kod kreskowy produktu
     */
    suspend fun submitProductRequest(name: String, barcode: String) {
        val userId = auth.currentUser?.uid ?: "anon"
        val request = hashMapOf(
            "name" to name,
            "barcode" to barcode,
            "requestedBy" to userId,
            "status" to "pending",
            "timestamp" to FieldValue.serverTimestamp()
        )
        firestore.collection("product_requests").add(request).await()
    }

    /**
     * Zatwierdza zgłoszenie produktu — dodaje nowy produkt do kolekcji `products`
     * i usuwa powiązane zgłoszenie z kolekcji `product_requests`.
     *
     * @param requestId identyfikator dokumentu zgłoszenia do zatwierdzenia
     * @param productName nazwa nowego produktu
     * @param barcode kod kreskowy nowego produktu
     * @param category kategoria nowego produktu
     */
    suspend fun approveProduct(requestId: String, productName: String, barcode: String, category: String) {
        val newProduct = hashMapOf(
            "name" to productName,
            "barcodes" to listOf(barcode),
            "category" to category,
            "unit" to "szt."
        )
        firestore.collection("products").add(newProduct).await()
        firestore.collection("product_requests").document(requestId).delete().await()
    }

    /**
     * Dodaje nowy kod kreskowy do listy kodów istniejącego produktu.
     *
     * @param productId identyfikator produktu w kolekcji `products`
     * @param newBarcode nowy kod kreskowy do przypisania
     */
    suspend fun addBarcodeToExistingProduct(productId: String, newBarcode: String) {
        firestore.collection("products").document(productId)
            .update("barcodes", FieldValue.arrayUnion(newBarcode))
            .await()
    }

    /**
     * Zatwierdza zgłoszenie produktu na podstawie pełnego obiektu [Product].
     *
     * Używa operacji wsadowej (batch), która atomowo dodaje nowy produkt do kolekcji `products`
     * i usuwa zgłoszenie z kolekcji `product_requests`.
     * Jeśli [Product.id] jest niepuste, dokument zostanie zapisany pod tym identyfikatorem;
     * w przeciwnym razie Firestore nada automatyczny identyfikator.
     *
     * @param requestId identyfikator dokumentu zgłoszenia do usunięcia po zatwierdzeniu
     * @param product obiekt [Product] z danymi nowego produktu do zapisania w bazie
     */
    suspend fun approveProductRequest(requestId: String, product: Product) {
        val batch = firestore.batch()

        val newProductRef = if (product.id.isNotBlank()) {
            firestore.collection("products").document(product.id)
        } else {
            firestore.collection("products").document()
        }

        val productData = hashMapOf(
            "name" to product.name,
            "category" to product.category,
            "unit" to product.unit,
            "barcodes" to product.barcodes
        )

        batch.set(newProductRef, productData)

        val requestRef = firestore.collection("product_requests").document(requestId)
        batch.delete(requestRef)

        batch.commit().await()
    }

    /**
     * Odrzuca zgłoszenie produktu — usuwa je z kolekcji `product_requests`.
     *
     * @param requestId identyfikator dokumentu zgłoszenia do usunięcia
     */
    suspend fun rejectProductRequest(requestId: String) {
        firestore.collection("product_requests").document(requestId)
            .delete()
            .await()
    }
}
