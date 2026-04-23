package com.example.posilkuz.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.posilkuz.ui.theme.ThemeMode

enum class ProfileSubScreen {
    MAIN,
    SETTINGS,
    DISPLAY
}

@Composable
fun ProfileScreen(
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    // Usunięte: onNavigateToHome/Pantry/Recipes — nawigację obsługuje teraz Pager w MainActivity
    innerPadding: PaddingValues = PaddingValues()
) {
    var currentSubScreen by remember { mutableStateOf(ProfileSubScreen.MAIN) }

    BackHandler(enabled = currentSubScreen != ProfileSubScreen.MAIN) {
        currentSubScreen = when (currentSubScreen) {
            ProfileSubScreen.DISPLAY -> ProfileSubScreen.SETTINGS
            ProfileSubScreen.SETTINGS -> ProfileSubScreen.MAIN
            else -> ProfileSubScreen.MAIN
        }
    }

    // Bez własnego Scaffold i NavigationBar — pasek jest w MainPagerScreen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
    ) {
        when (currentSubScreen) {
            ProfileSubScreen.MAIN -> ProfileMainView(
                onNavigateToSettings = { currentSubScreen = ProfileSubScreen.SETTINGS }
            )
            ProfileSubScreen.SETTINGS -> SettingsView(
                onBack = { currentSubScreen = ProfileSubScreen.MAIN },
                onNavigateToDisplay = { currentSubScreen = ProfileSubScreen.DISPLAY }
            )
            ProfileSubScreen.DISPLAY -> DisplaySettingsView(
                currentTheme = currentTheme,
                onThemeChange = onThemeChange,
                onBack = { currentSubScreen = ProfileSubScreen.SETTINGS }
            )
        }
    }
}

@Composable
fun ProfileMainView(onNavigateToSettings: () -> Unit) {
    Text(
        "Mój Profil",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(bottom = 24.dp)
    )
    ListItem(
        headlineContent = { Text("Ustawienia") },
        leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) },
        trailingContent = { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null) },
        modifier = Modifier.clickable { onNavigateToSettings() }
    )
}

@Composable
fun SettingsView(onBack: () -> Unit, onNavigateToDisplay: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz") }
        Text("Ustawienia", style = MaterialTheme.typography.headlineSmall)
    }
    ListItem(
        headlineContent = { Text("Wyświetlanie") },
        leadingContent = { Icon(Icons.Default.DarkMode, contentDescription = "Ikona księżyca") },
        trailingContent = { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null) },
        modifier = Modifier.clickable { onNavigateToDisplay() }
    )
}

@Composable
fun DisplaySettingsView(
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz") }
        Text("Wyświetlanie", style = MaterialTheme.typography.headlineSmall)
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            ThemeOptionRow("Systemowy", currentTheme == ThemeMode.SYSTEM) { onThemeChange(ThemeMode.SYSTEM) }
            ThemeOptionRow("Jasny", currentTheme == ThemeMode.LIGHT) { onThemeChange(ThemeMode.LIGHT) }
            ThemeOptionRow("Ciemny", currentTheme == ThemeMode.DARK) { onThemeChange(ThemeMode.DARK) }
        }
    }
}

@Composable
fun ThemeOptionRow(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text)
    }
}