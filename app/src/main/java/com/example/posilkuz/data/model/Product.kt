package com.example.posilkuz.data.model

/**
 * Model danych reprezentujący produkt spożywczy w systemie.
 *
 * Używany zarówno do pobierania produktów z Firebase Firestore,
 * jak i do zarządzania zawartością spiżarni użytkownika.
 * Pusty konstruktor jest wymagany przez bibliotekę Firestore
 * do automatycznej deserializacji dokumentów.
 *
 * @property id unikalny identyfikator dokumentu w kolekcji Firestore
 * @property name nazwa produktu, np. „Cukier"
 * @property category kategoria produktu, np. „Sypkie", „Nabiał"
 * @property unit jednostka miary produktu, np. „g", „ml", „szt"
 * @property barcodes lista kodów kreskowych powiązanych z produktem
 */
data class Product(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val unit: String = "",
    val barcodes: List<String> = emptyList()
)
