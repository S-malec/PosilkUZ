package com.example.posilkuz.data

import android.content.Context
import com.google.common.reflect.TypeToken
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

fun importRecipesFromJson(context: Context) {
    val db = FirebaseFirestore.getInstance()
    val gson = Gson()

    try {
        val jsonString = context.assets.open("recipes.json")
            .bufferedReader()
            .use { it.readText() }

        // Parsowanie JSONa na listę map
        val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
        val recipes: List<Map<String, Any>> = gson.fromJson(jsonString, listType)

        // Batch (szybsze wrzucenie danych)
        val batch = db.batch()

        recipes.forEach { recipe ->
            val title = recipe["title"] as String

            // ID dokumentu na podstawie title (opcjonalnie możesz zrobić slug)
            val docId = title
                .lowercase()
                .replace(" ", "_")

            val docRef = db.collection("recipes").document(docId)
            batch.set(docRef, recipe)
        }

        batch.commit()
            .addOnSuccessListener {
                println("Sukces: Wszystkie przepisy zostały dodane!")
            }
            .addOnFailureListener { e ->
                println("Błąd podczas importu: ${e.message}")
            }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}