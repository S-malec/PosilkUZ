package com.example.posilkuz.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.posilkuz.R

/**
 * Ekran logowania użytkownika.
 *
 * Wyświetla formularz z polami e-mail i hasło oraz przycisk logowania.
 * W trakcie weryfikacji pokazuje animowany wskaźnik ładowania z informacją
 * o sprawdzaniu połączenia z Firebase. Po zakończeniu operacji wyświetla
 * komunikat o sukcesie lub błędzie.
 *
 * @param viewModel instancja [AuthViewModel] obsługująca logikę logowania
 * @param onNavigateToRegister wywołanie zwrotne nawigacji do ekranu rejestracji
 */
@Composable
fun LoginScreen(viewModel: AuthViewModel, onNavigateToRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "PosiłkUZ \uD83E\uDD57", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = password, onValueChange = { password = it }, label = { Text("Hasło") }, visualTransformation = PasswordVisualTransformation())

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.login(email, password) }) {
            Text("Zaloguj się")
        }

        AnimatedVisibility(visible = viewModel.isVerifying) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.verifying_connection),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    text = stringResource(R.string.firebase_app_check),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        TextButton(onClick = onNavigateToRegister) {
            Text(text = stringResource(R.string.no_account))
        }

        when (val state = viewModel.authState) {
            is AuthResult.Error -> Text(state.message.asString(context), color = Color.Red)
            is AuthResult.Success -> Text(text = stringResource(R.string.login_success), color = Color.Green)
            else -> {}
        }
    }
}
