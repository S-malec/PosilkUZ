package com.example.posilkuz.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

    // Stan ogólny (Sukces/Błąd)
    var authState by mutableStateOf<AuthResult>(AuthResult.Idle)
        private set

    // Stan dla animacji weryfikacji
    var isVerifying by mutableStateOf(false)
        private set

    /**
     * Logowanie użytkownika
     */
    fun login(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            authState = AuthResult.Error("Uzupełnij wszystkie pola")
            return
        }

        viewModelScope.launch {
            try {
                isVerifying = true
                authState = AuthResult.Loading

                delay(1500) // Efekt wizualny weryfikacji

                auth.signInWithEmailAndPassword(email, pass).await()

                authState = AuthResult.Success
            } catch (e: Exception) {
                authState = AuthResult.Error(e.localizedMessage ?: "Błąd logowania")
            } finally {
                isVerifying = false
            }
        }
    }

    /**
     * Rejestracja nowego użytkownika z zapisem danych w Firestore
     */
    fun register(email: String, pass: String, nickname: String) {
        if (nickname.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            authState = AuthResult.Error("Uzupełnij wszystkie pola")
            return
        }

        viewModelScope.launch {
            try {
                isVerifying = true
                authState = AuthResult.Loading

                delay(2000) // Symulacja App Check

                // 1. Tworzenie konta w Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val userId = authResult.user?.uid ?: throw Exception("Nie udało się pobrać ID użytkownika")

                // 2. Przygotowanie danych (Wszystko w jednej mapie, by uniknąć nadpisywania)
                val userMap = hashMapOf(
                    "nickname" to nickname,
                    "email" to email,
                    "pantry" to emptyList<String>(),
                    "createdAt" to com.google.firebase.Timestamp.now()
                )

                // 3. Zapis do Firestore (Używamy await(), więc kod czeka na zakończenie zapisu)
                db.collection("users").document(userId).set(userMap).await()

                authState = AuthResult.Success
            } catch (e: Exception) {
                // Jeśli np. Auth się uda, ale Firestore padnie, warto tu obsłużyć wycofanie zmian
                authState = AuthResult.Error(e.localizedMessage ?: "Błąd rejestracji")
            } finally {
                isVerifying = false
            }
        }
    }
}