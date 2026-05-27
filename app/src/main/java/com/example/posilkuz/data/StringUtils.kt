package com.example.posilkuz.data

/**
 * Normalizuje ciąg znaków przez zastąpienie polskich liter diakrytycznych ich
 * łacińskimi odpowiednikami oraz zamianę wszystkich znaków na małe litery.
 *
 * Używana przede wszystkim do porównywania i wyszukiwania tekstu bez uwzględniania
 * polskich znaków specjalnych (np. „ą" → „a", „ę" → „e", „ł" → „l").
 *
 * @return znormalizowany ciąg znaków bez polskich znaków diakrytycznych, zapisany małymi literami
 */
fun String.normalizePolish(): String {
    val map = mapOf(
        'ą' to 'a', 'ć' to 'c', 'ę' to 'e', 'ł' to 'l', 'ń' to 'n',
        'ó' to 'o', 'ś' to 's', 'ź' to 'z', 'ż' to 'z',
        'Ą' to 'a', 'Ć' to 'c', 'Ę' to 'e', 'Ł' to 'l', 'Ń' to 'n',
        'Ó' to 'o', 'Ś' to 's', 'Ź' to 'z', 'Ż' to 'z'
    )
    return this.map { map[it] ?: it.lowercaseChar() }.joinToString("")
}
