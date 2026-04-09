package com.example.posilkuz.data

import android.content.Context
import com.google.common.reflect.TypeToken
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

fun importProductsFromJson(context: Context) {
    val db = FirebaseFirestore.getInstance()
    val gson = Gson()

    try {
        val jsonString = context.assets.open("dane_json.json").bufferedReader().use { it.readText() }

        // 2. Sparsuj JSON na listę map (lub na listę Twoich modeli Product)
        val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
        val products: List<Map<String, Any>> = gson.fromJson(jsonString, listType)

        // 3. Wyślij do Firestore (używając ID z JSONa jako ID dokumentu)
        val batch = db.batch() // Używamy batcha, żeby wysłać wszystko na raz (szybciej)

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