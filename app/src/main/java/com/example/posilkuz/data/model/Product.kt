package com.example.posilkuz.data.model

data class Product(
    val id: String = "",         // ID dokumentu z Firestore
    val name: String = "",       // np. "Cukier"
    val category: String = "",   // np. "Sypkie", "Nabiał"
    val unit: String = "",       // np. "g", "ml", "szt"
    val barcode: String? = null
)