package com.example.posilkuz.ui.translation

import android.content.Context
import androidx.annotation.StringRes

sealed class TranslationHelper {
    data class DynamicString(val value: String) : TranslationHelper()
    data class StringResource(
        @StringRes val resId: Int,
        val args: Array<out Any> = emptyArray()
    ) : TranslationHelper()

    fun asString(context: Context): String = when (this) {
        is DynamicString -> value
        is StringResource -> context.getString(resId, *args)
    }
}

fun android.content.Context.getDynamicString(prefix: String, key: String, fallback: String): String {
    val sanitizedKey = "${prefix}_${key.lowercase()}"
        .replace(" ", "_")
        .replace("ą", "a")
        .replace("ć", "c")
        .replace("ę", "e")
        .replace("ł", "l")
        .replace("ń", "n")
        .replace("ó", "o")
        .replace("ś", "s")
        .replace("ź", "z")
        .replace("ż", "z")
        .replace(Regex("[^a-z0-9_]"), "")

    val resId = resources.getIdentifier(sanitizedKey, "string", packageName)

    return if (resId != 0) getString(resId) else fallback
}