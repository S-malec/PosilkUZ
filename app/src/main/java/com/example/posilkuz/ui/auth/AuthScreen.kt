package com.example.posilkuz.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.posilkuz.ui.home.HomeScreen
import com.google.firebase.auth.FirebaseAuth

/**
 * Ekran autoryzacji zarządzający przełączaniem między widokiem logowania a rejestracji.
 *
 * Obserwuje stan [AuthViewModel.authState] i po pomyślnej autoryzacji
 * wywołuje [onAuthSuccess], aby przekazać sterowanie do ekranu głównego.
 *
 * @param modifier modyfikator Compose stosowany do kontenera ekranu
 * @param viewModel instancja [AuthViewModel] obsługująca logikę autoryzacji
 * @param onAuthSuccess wywołanie zwrotne wywoływane po pomyślnym zalogowaniu lub rejestracji
 */
@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(),
    onAuthSuccess: () -> Unit
) {
    var isLoginScreen by remember { mutableStateOf(true) }

    LaunchedEffect(viewModel.authState) {
        if (viewModel.authState is AuthResult.Success) {
            onAuthSuccess()
        }
    }

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
