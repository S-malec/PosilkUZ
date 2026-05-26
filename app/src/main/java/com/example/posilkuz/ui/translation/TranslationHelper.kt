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