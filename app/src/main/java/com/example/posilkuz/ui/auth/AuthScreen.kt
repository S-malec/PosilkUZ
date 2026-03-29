package com.example.posilkuz.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.posilkuz.ui.home.HomeScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthScreen(modifier: Modifier = Modifier, viewModel: AuthViewModel = viewModel()) {
    var isLoginScreen by remember { mutableStateOf(true) }

    val currentUser by remember(viewModel.authState) {
        derivedStateOf { FirebaseAuth.getInstance().currentUser }
    }

    // Główny kontener na CAŁY ekran
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = modifier.fillMaxSize()) {
            if (currentUser != null) {
                HomeScreen(onLogout = { viewModel.logout() })
            } else {
                if (isLoginScreen) {
                    LoginScreen(
                        viewModel = viewModel,
                        onNavigateToRegister = { isLoginScreen = false }
                    )
                } else {
                    RegisterScreen(
                        viewModel = viewModel,
                        onNavigateToLogin = { isLoginScreen = true }
                    )
                }
            }
        }
    }
}