package com.example.posilkuz.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.posilkuz.ui.home.HomeScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
    onAuthSuccess: () -> Unit // Dodajemy ten parametr!
) {
    var isLoginScreen by remember { mutableStateOf(true) }

    // Reagujemy na zmianę stanu w ViewModelu
    // Jeśli stan zmieni się na Success, wywołujemy funkcję nawigacji do Home
    LaunchedEffect(viewModel.authState) {
        if (viewModel.authState is AuthResult.Success) {
            onAuthSuccess()
        }
    }

    // Główny kontener - usuwamy stąd HomeScreen!
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = modifier.fillMaxSize()) {
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