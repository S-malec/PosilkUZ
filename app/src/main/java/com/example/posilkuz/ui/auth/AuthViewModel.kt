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

class AuthViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

    // Stan ogólny (Sukces/Błąd)
    var authState by mutableStateOf<AuthResult>(AuthResult.Idle)
        private set

    // Stan dla animacji "Cloudflare / App Check"
    var isVerifying by mutableStateOf(false)
        private set

    fun login(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) return

        viewModelScope.launch {
            // 1. Startujemy animację weryfikacji
            isVerifying = true
            authState = AuthResult.Loading

            // 2. Krótkie opóźnienie dla efektu "sprawdzania bezpiecznego połączenia"
            delay(1500)

            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    // 3. Wyłączamy animację po zakończeniu operacji
                    isVerifying = false
                    authState = if (task.isSuccessful) AuthResult.Success
                    else AuthResult.Error(task.exception?.message ?: "Błąd logowania")
                }
        }
    }

    fun register(email: String, pass: String, nickname: String) {
        if (nickname.isEmpty()) {
            authState = AuthResult.Error("Nick nie może być pusty")
            return
        }

        viewModelScope.launch {
            // 1. Startujemy animację weryfikacji
            isVerifying = true
            authState = AuthResult.Loading

            // 2. Symulacja weryfikacji App Check
            delay(2000)

            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        val userMap = hashMapOf("nickname" to nickname, "email" to email)

                        if (userId != null) {
                            db.collection("users").document(userId).set(userMap)
                                .addOnSuccessListener {
                                    isVerifying = false // Wyłączamy przy sukcesie zapisu
                                    authState = AuthResult.Success
                                }
                                .addOnFailureListener {
                                    isVerifying = false
                                    authState = AuthResult.Error("Błąd zapisu danych użytkownika")
                                }
                        }
                    } else {
                        isVerifying = false // Wyłączamy przy błędzie rejestracji
                        authState = AuthResult.Error(task.exception?.message ?: "Błąd rejestracji")
                    }
                }
        }
    }

    fun logout() {
        auth.signOut()
        authState = AuthResult.Idle
    }
}