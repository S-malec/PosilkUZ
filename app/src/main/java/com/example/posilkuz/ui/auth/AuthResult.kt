package com.example.posilkuz.ui.auth

import com.example.posilkuz.ui.translation.TranslationHelper

/**
 * Zapieczętowana klasa reprezentująca możliwe stany operacji autoryzacji.
 *
 * Używana w [AuthViewModel] do komunikowania wyniku operacji logowania
 * lub rejestracji do warstwy UI.
 */
sealed class AuthResult {

    /** Stan początkowy — żadna operacja autoryzacji nie została jeszcze zainicjowana. */
    object Idle : AuthResult()

    /** Operacja autoryzacji jest w toku. */
    object Loading : AuthResult()

    /** Operacja autoryzacji zakończyła się sukcesem. */
    object Success : AuthResult()

    /**
     * Operacja autoryzacji zakończyła się błędem.
     *
     * @property message komunikat błędu przeznaczony do wyświetlenia użytkownikowi
     */
    data class Error(val message: TranslationHelper.StringResource) : AuthResult()
}
