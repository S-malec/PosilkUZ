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
}