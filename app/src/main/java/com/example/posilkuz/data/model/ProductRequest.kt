package com.example.posilkuz.data.model

/**
 * Model danych reprezentujący zgłoszenie nowego produktu przez użytkownika.
 *
 * Przechowywany w kolekcji `product_requests` w Firebase Firestore.
 * Administrator może zatwierdzić zgłoszenie (dodać produkt do bazy)
 * lub je odrzucić.
 *
 * @property id unikalny identyfikator dokumentu zgłoszenia w Firestore
 * @property name proponowana nazwa nowego produktu
 * @property barcode kod kreskowy zgłoszonego produktu
 * @property requestedBy identyfikator użytkownika, który złożył zgłoszenie
 * @property status aktualny status zgłoszenia; domyślnie `"pending"`
 */
data class ProductRequest(
    val id: String = "",
    val name: String = "",
    val barcode: String = "",
    val requestedBy: String = "",
    val status: String = "pending"
)
