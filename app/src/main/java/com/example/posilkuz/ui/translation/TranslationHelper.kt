package com.example.posilkuz.ui.translation

import android.content.Context
import androidx.annotation.StringRes

/**
 * Zapieczętowana klasa reprezentująca tekstowy zasób UI niezależny od kontekstu Compose.
 *
 * Rozwiązuje problem przekazywania tekstów z warstwy ViewModel do warstwy UI:
 * ViewModel nie powinien posiadać referencji do [Context], więc zamiast ciągów znaków
 * przekazuje obiekty [TranslationHelper], które są rozwijane do `String` dopiero w UI.
 *
 * Obsługuje dwa źródła tekstu:
 * - [DynamicString] — tekst znany już w momencie tworzenia (np. komunikat błędu z API),
 * - [StringResource] — identyfikator zasobu tekstowego z `res/values/strings.xml`.
 */
sealed class TranslationHelper {

    /**
     * Dynamiczny tekst znany w momencie tworzenia, np. komunikat błędu z wyjątku.
     *
     * @property value gotowy ciąg znaków do wyświetlenia
     */
    data class DynamicString(val value: String) : TranslationHelper()

    /**
     * Zasób tekstowy identyfikowany przez identyfikator zasobu Androida.
     *
     * @property resId identyfikator zasobu tekstowego z `R.string.*`
     * @property args opcjonalne argumenty formatowania przekazywane do [Context.getString]
     */
    data class StringResource(
        @StringRes val resId: Int,
        val args: Array<out Any> = emptyArray()
    ) : TranslationHelper()

    /**
     * Rozwija obiekt do ciągu znaków przy użyciu podanego kontekstu.
     *
     * @param context kontekst potrzebny do rozwinięcia zasobu tekstowego
     * @return gotowy do wyświetlenia ciąg znaków
     */
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