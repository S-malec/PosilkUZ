package com.example.posilkuz.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.posilkuz.ui.translation.TranslationHelper
import com.example.posilkuz.R

/**
 * Niestandardowy komponent snackbara wyświetlający powiadomienie o sukcesie lub błędzie.
 *
 * Automatycznie dobiera kolor tła i ikonę na podstawie treści wiadomości — jeśli komunikat
 * zawiera słowo odpowiadające kluczowi `R.string.error` lub `R.string.failed`,
 * snackbar jest renderowany w kolorze czerwonym z ikoną błędu; w pozostałych przypadkach
 * wyświetlany jest w kolorze zielonym z ikoną sukcesu.
 *
 * @param data dane snackbara dostarczane przez [SnackbarHostState], zawierające treść komunikatu
 */
@Composable
fun AppSnackbar(data: SnackbarData) {
    val context = LocalContext.current
    val isError = data.visuals.message.contains(TranslationHelper.StringResource(R.string.error).asString(context), ignoreCase = true) ||
            data.visuals.message.contains(TranslationHelper.StringResource(R.string.failed).asString(context), ignoreCase = true)

    val containerColor = if (isError) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
    val contentColor = if (isError) Color(0xFFC62828) else Color(0xFF2E7D32)
    val icon = if (isError) Icons.Default.Error else Icons.Default.CheckCircle

    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = contentColor)
            Spacer(Modifier.width(12.dp))
            Text(
                text = data.visuals.message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
