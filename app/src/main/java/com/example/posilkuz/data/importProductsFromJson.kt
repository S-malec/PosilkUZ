package com.example.posilkuz.data

import android.content.Context
import com.google.common.reflect.TypeToken
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

/**
 * Importuje produkty z lokalnego pliku JSON do kolekcji `products` w Firebase Firestore.
 *
 * Odczytuje plik `dane_json.json` z folderu `assets`, parsuje go jako listę map,
 * a następnie wysyła wszystkie rekordy do Firestore za pomocą operacji wsadowej (batch).
 * ID dokumentu Firestore pochodzi z pola `id` w JSONie.
 *
 * Funkcja jest przeznaczona do jednorazowego zasilenia bazy danych danymi początkowymi.
 *
 * @param context kontekst aplikacji potrzebny do odczytu pliku z `assets`
 */
fun importProductsFromJson(context: Context) {
    val db = FirebaseFirestore.getInstance()
    val gson = Gson()

    try {
        val jsonString = context.assets.open("dane_json.json").bufferedReader().use { it.readText() }

        val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
        val products: List<Map<String, Any>> = gson.fromJson(jsonString, listType)

        val batch = db.batch()

        products.forEach { product ->
            val id = product["id"] as String
            val docRef = db.collection("products").document(id)
            batch.set(docRef, product)
        }

        batch.commit()
            .addOnSuccessListener { println("Sukces: Wszystkie produkty zostały dodane!") }
            .addOnFailureListener { e -> println("Błąd podczas importu: ${e.message}") }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}
