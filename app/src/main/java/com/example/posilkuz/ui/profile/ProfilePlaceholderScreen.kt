package com.example.posilkuz.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.posilkuz.R

/**
 * Ekran zastępczy profilu użytkownika wyświetlający komunikat „wkrótce dostępne".
 *
 * Używany jako tymczasowy placeholder przed zaimplementowaniem pełnej funkcjonalności
 * profilu użytkownika.
 *
 * @param innerPadding padding wewnętrzny przekazywany z zewnętrznego [Scaffold]
 */
@Composable
fun ProfilePlaceholderScreen(
    innerPadding: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.profile),
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = stringResource(R.string.coming_soon),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}
