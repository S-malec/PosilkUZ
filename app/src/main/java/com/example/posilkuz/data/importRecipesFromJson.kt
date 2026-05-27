package com.example.posilkuz.data

import android.content.Context
import com.google.common.reflect.TypeToken
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

/**
 * Importuje przepisy z lokalnego pliku JSON do kolekcji `recipes` w Firebase Firestore.
 *
 * Odczytuje plik `recipes.json` z folderu `assets`, parsuje go jako listę map,
 * a następnie wysyła wszystkie rekordy do Firestore za pomocą operacji wsadowej (batch).
 * ID dokumentu Firestore jest generowane na podstawie pola `title` — tytuł przepisu
 * jest zamieniony na małe litery, a spacje zastąpione znakiem podkreślenia.
 *
 * Funkcja jest przeznaczona do jednorazowego zasilenia bazy danych danymi początkowymi.
 *
 * @param context kontekst aplikacji potrzebny do odczytu pliku z `assets`
 */
fun importRecipesFromJson(context: Context) {
    val db = FirebaseFirestore.getInstance()
    val gson = Gson()

    try {
        val jsonString = context.assets.open("recipes.json")
            .bufferedReader()
            .use { it.readText() }

        val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
        val recipes: List<Map<String, Any>> = gson.fromJson(jsonString, listType)

        val batch = db.batch()

        recipes.forEach { recipe ->
            val title = recipe["title"] as String

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
