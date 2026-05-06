package com.example.posilkuz.data.repository

import com.example.posilkuz.data.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val productsCollection = firestore.collection("products")

    // Pobiera wszystkie dostępne produkty w systemie (do wyboru z listy)
    suspend fun getAllProducts(): List<Product> {
        return try {
            val snapshot = productsCollection.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id) // Przepisujemy ID dokumentu do pola id w klasie
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Pobiera ID produktów, które posiada zalogowany użytkownik
    suspend fun getUserPantryIds(): List<String> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            // Zakładamy, że w dokumencie użytkownika jest pole "pantry" typu List<String>
            @Suppress("UNCHECKED_CAST")
            document.get("pantry") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // W ProductRepository.kt
    fun getUserPantryIdsFlow(): Flow<Set<String>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptySet())
            return@callbackFlow
        }

        // Słuchamy dokumentu użytkownika (bo tam jest tablica "pantry")
        val listener = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Błąd Firestore: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    // Pobieramy pole "pantry" jako listę Stringów
                    @Suppress("UNCHECKED_CAST")
                    val pantryList = snapshot.get("pantry") as? List<String> ?: emptyList()

                    println("Flow wykrył zmianę w bazie! Nowe dane: $pantryList")
                    trySend(pantryList.toSet())
                } else {
                    trySend(emptySet())
                }
            }

        awaitClose {
            println("Zamykanie listenera Firestore")
            listener.remove()
        }
    }

    // Dodaje/Usuwa produkt ze spiżarni użytkownika
    suspend fun updatePantry(newPantry: List<String>) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .update("pantry", newPantry).await()
    }

    suspend fun addProductToPantry(productId: String) {
        val userId = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(userId)
                .update("pantry", FieldValue.arrayUnion(productId))
                .await()
        } catch (e: Exception) {
            // Obsłuż błąd, np. logując go
        }
    }

    suspend fun removeProductFromPantry(productId: String) {
        val userId = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(userId)
                .update("pantry", FieldValue.arrayRemove(productId))
                .await()
        } catch (e: Exception) {
            // Obsłuż błąd
        }
    }

    // W ProductRepository.kt
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

    // Funkcja dla admina do zatwierdzenia
    suspend fun approveProduct(requestId: String, productName: String, barcode: String, category: String) {
        // 1. Dodaj produkt do głównej bazy 'products'
        val newProduct = hashMapOf(
            "name" to productName,
            "barcodes" to listOf(barcode),
            "category" to category,
            "unit" to "szt."
        )
        firestore.collection("products").add(newProduct).await()

        // 2. Usuń prośbę lub zmień status
        firestore.collection("product_requests").document(requestId).delete().await()
    }

    suspend fun addBarcodeToExistingProduct(productId: String, newBarcode: String) {
        firestore.collection("products").document(productId)
            .update("barcodes", FieldValue.arrayUnion(newBarcode))
            .await()
    }

    suspend fun approveProductRequest(requestId: String, product: Product) {
        val batch = firestore.batch()

        // Jeśli admin podał ID, używamy go. Jeśli nie, tworzymy nowy dokument z auto-ID.
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

        // Usunięcie zgłoszenia po pomyślnym dodaniu produktu
        val requestRef = firestore.collection("product_requests").document(requestId)
        batch.delete(requestRef)

        batch.commit().await()
    }

    // Funkcja do odrzucania zgłoszenia
    suspend fun rejectProductRequest(requestId: String) {
        firestore.collection("product_requests").document(requestId)
            .delete()
            .await()
    }
}