package com.example.posilkuz.data.repository

import com.example.posilkuz.data.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
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