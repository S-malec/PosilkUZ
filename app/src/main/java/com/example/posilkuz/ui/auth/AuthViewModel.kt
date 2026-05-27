package com.example.posilkuz.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.posilkuz.ui.translation.TranslationHelper
import com.example.posilkuz.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel obsługujący logikę autoryzacji użytkownika.
 *
 * Zarządza stanem logowania i rejestracji przez Firebase Authentication,
 * a po rejestracji zapisuje dane użytkownika (nick, e-mail, pusta spiżarnia)
 * w kolekcji `users` w Firebase Firestore.
 */
class AuthViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

    /**
     * Aktualny stan operacji autoryzacji, np. [AuthResult.Idle], [AuthResult.Loading],
     * [AuthResult.Success] lub [AuthResult.Error].
     */
    var authState by mutableStateOf<AuthResult>(AuthResult.Idle)
        private set

    /**
     * Flaga informująca czy trwa animacja weryfikacji połączenia z Firebase.
     * Widoczna w UI jako wskaźnik ładowania z komunikatem.
     */
    var isVerifying by mutableStateOf(false)
        private set

    /**
     * Loguje użytkownika za pomocą adresu e-mail i hasła przez Firebase Authentication.
     *
     * Przed wysłaniem żądania waliduje, czy pola nie są puste. Ustawia [isVerifying]
     * na `true` na czas operacji, a po jej zakończeniu aktualizuje [authState].
     *
     * @param email adres e-mail użytkownika
     * @param pass hasło użytkownika
     */
    fun login(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            authState = AuthResult.Error(TranslationHelper.StringResource(R.string.fill_all_fields))
            return
        }

        viewModelScope.launch {
            try {
                isVerifying = true
                authState = AuthResult.Loading

                delay(1500)

                auth.signInWithEmailAndPassword(email, pass).await()

                authState = AuthResult.Success
            } catch (e: Exception) {
                authState = AuthResult.Error(
                    (if (e.localizedMessage != null)
                        TranslationHelper.DynamicString(e.localizedMessage)
                    else
                        TranslationHelper.StringResource(R.string.fill_all_fields)) as TranslationHelper.StringResource
                )
            } finally {
                isVerifying = false
            }
        }
    }

    /**
     * Rejestruje nowego użytkownika i zapisuje jego dane w Firebase Firestore.
     *
     * Przed wysłaniem żądania waliduje, czy wszystkie pola są wypełnione.
     * Po pomyślnym utworzeniu konta w Firebase Authentication zapisuje do Firestore
     * dokument użytkownika zawierający nick, e-mail, pustą spiżarnię i znacznik czasu.
     *
     * @param email adres e-mail nowego użytkownika
     * @param pass hasło nowego użytkownika
     * @param nickname pseudonim wyświetlany w aplikacji
     */
    fun register(email: String, pass: String, nickname: String) {
        if (nickname.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            authState = AuthResult.Error(TranslationHelper.StringResource(R.string.fill_all_fields))
            return
        }

        viewModelScope.launch {
            try {
                isVerifying = true
                authState = AuthResult.Loading

                delay(2000)

                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val userId = authResult.user?.uid ?: throw Exception("uid_null")

                val userMap = hashMapOf(
                    "nickname" to nickname,
                    "email" to email,
                    "pantry" to emptyList<String>(),
                    "createdAt" to com.google.firebase.Timestamp.now()
                )

                db.collection("users").document(userId).set(userMap).await()

                authState = AuthResult.Success
            } catch (e: Exception) {
                authState = AuthResult.Error(
                    when (e.message) {
                        "uid_null" -> TranslationHelper.StringResource(R.string.fetch_user_id_error)
                        else -> TranslationHelper.DynamicString(e.localizedMessage ?: "")
                            .takeIf { e.localizedMessage != null }
                            ?: TranslationHelper.StringResource(R.string.register_error)
                    } as TranslationHelper.StringResource
                )
            } finally {
                isVerifying = false
            }
        }
    }
}
