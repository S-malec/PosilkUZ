package com.example.posilkuz.data.model

data class Recipe(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val instructions: String = "",
    // Lista ID produktów potrzebnych do przepisu
    val ingredientIds: List<String> = emptyList(),
    // Opcjonalnie: mapa ilości (id_produktu to klucz, ilość to wartość)
    val ingredientsAmount: Map<String, String> = emptyMap()
)