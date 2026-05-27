package com.example.posilkuz.data.model

/**
 * Model danych reprezentujący przepis kulinarny.
 *
 * Przechowywany w kolekcji `recipes` w Firebase Firestore.
 * Pusty konstruktor jest wymagany przez bibliotekę Firestore
 * do automatycznej deserializacji dokumentów.
 *
 * @property id unikalny identyfikator dokumentu przepisu w Firestore
 * @property title tytuł przepisu, np. „Jajecznica"
 * @property description krótki opis przepisu
 * @property instructions pełna treść instrukcji przygotowania potrawy
 * @property ingredientIds lista identyfikatorów produktów ([Product.id]) potrzebnych do wykonania przepisu
 * @property ingredientsAmount mapa ilości składników, gdzie kluczem jest identyfikator produktu,
 *   a wartością ilość w czytelnej formie, np. `"jajko" to "3 szt."`
 */
data class Recipe(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val instructions: String = "",
    val ingredientIds: List<String> = emptyList(),
    val ingredientsAmount: Map<String, String> = emptyMap()
)
