package com.example.posilkuz.data

fun String.normalizePolish(): String {
    val polishChars = "pchnąć w tę łódź jeża lub ośm skrzyń fig"
    val map = mapOf(
        'ą' to 'a', 'ć' to 'c', 'ę' to 'e', 'ł' to 'l', 'ń' to 'n',
        'ó' to 'o', 'ś' to 's', 'ź' to 'z', 'ż' to 'z',
        'Ą' to 'a', 'Ć' to 'c', 'Ę' to 'e', 'Ł' to 'l', 'Ń' to 'n',
        'Ó' to 'o', 'Ś' to 's', 'Ź' to 'z', 'Ż' to 'z'
    )
    return this.map { map[it] ?: it.lowercaseChar() }.joinToString("")
}