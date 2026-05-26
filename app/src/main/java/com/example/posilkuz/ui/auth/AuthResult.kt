package com.example.posilkuz.ui.auth

import com.example.posilkuz.ui.translation.TranslationHelper

sealed class AuthResult {
    object Idle : AuthResult()
    object Loading : AuthResult()
    object Success : AuthResult()
    data class Error(val message: TranslationHelper.StringResource) : AuthResult()
}