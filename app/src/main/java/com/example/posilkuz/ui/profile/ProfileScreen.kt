package com.example.posilkuz.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.posilkuz.data.model.ProductRequest
import com.example.posilkuz.ui.theme.ThemeMode
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import com.example.posilkuz.data.model.Product
import com.example.posilkuz.data.repository.ProductRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

enum class ProfileSubScreen {
    MAIN,
    SETTINGS,
    DISPLAY,
    ADMIN_PANEL
}

@Composable
fun ProfileScreen(
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    innerPadding: PaddingValues = PaddingValues()
) {
    var currentSubScreen by remember { mutableStateOf(ProfileSubScreen.MAIN) }

    // Obsługa przycisku wstecz
    BackHandler(enabled = currentSubScreen != ProfileSubScreen.MAIN) {
        currentSubScreen = when (currentSubScreen) {
            ProfileSubScreen.DISPLAY -> ProfileSubScreen.SETTINGS
            ProfileSubScreen.ADMIN_PANEL -> ProfileSubScreen.MAIN
            ProfileSubScreen.SETTINGS -> ProfileSubScreen.MAIN
            else -> ProfileSubScreen.MAIN
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
    ) {
        when (currentSubScreen) {
            ProfileSubScreen.MAIN -> ProfileMainView(
                onNavigateToSettings = { currentSubScreen = ProfileSubScreen.SETTINGS },
                onNavigateToAdmin = { currentSubScreen = ProfileSubScreen.ADMIN_PANEL } // <-- Przekazanie akcji
            )
            ProfileSubScreen.ADMIN_PANEL -> AdminRequestsView(
                onBack = { currentSubScreen = ProfileSubScreen.MAIN }
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
fun ProfileMainView(onNavigateToSettings: () -> Unit, onNavigateToAdmin: () -> Unit) {
    Text("Mój Profil", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 24.dp))

    ListItem(
        headlineContent = { Text("Ustawienia") },
        leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) },
        trailingContent = { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null) },
        modifier = Modifier.clickable { onNavigateToSettings() }
    )

    // Przycisk widoczny dla administratora
    ListItem(
        headlineContent = { Text("Zatwierdź produkty") },
        leadingContent = { Icon(Icons.Default.FactCheck, contentDescription = null) },
        trailingContent = { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null) },
        modifier = Modifier.clickable { onNavigateToAdmin() }
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

@Composable
fun AdminRequestsView(
    onBack: () -> Unit,
    repository: ProductRepository = ProductRepository()
) {
    val requests = remember { mutableStateListOf<ProductRequest>() }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()

    // Słuchacz na żywo zgłoszeń
    LaunchedEffect(Unit) {
        val listener = db.collection("product_requests")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    requests.clear()
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ProductRequest::class.java)?.copy(id = doc.id)
                    }
                    requests.addAll(items)
                }
                isLoading = false
            }
        // Zatrzymaj słuchanie, gdy wyjdziesz z ekranu
    }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz") }
            Text("Zgłoszone produkty", style = MaterialTheme.typography.headlineSmall)
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (requests.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Brak nowych zgłoszeń", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(requests) { request ->
                    AdminRequestCard(
                        request = request,
                        onApprove = { requestId, finalProduct ->
                            scope.launch {
                                try {
                                    // Używamy nowej metody, która przyjmuje cały obiekt Product
                                    repository.approveProductRequest(
                                        requestId = requestId,
                                        product = finalProduct
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        },
                        onReject = { requestId ->
                            scope.launch {
                                repository.rejectProductRequest(requestId)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRequestCard(
    request: ProductRequest,
    onApprove: (requestId: String, finalProduct: Product) -> Unit,
    onReject: (String) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    // Stan formularza - inicjalizacja danymi z requestu
    var editedBarcode by remember { mutableStateOf(request.barcode) }
    var editedName by remember { mutableStateOf(request.name) }
    var editedId by remember { mutableStateOf("") } // Możesz tu wpisać sugerowane ID lub zostawić puste dla auto-gen
    var selectedCategory by remember { mutableStateOf("Inne") }
    var selectedUnit by remember { mutableStateOf("szt.") }

    val categories = listOf("Warzywa", "Owoce", "Nabiał", "Mięso", "Napoje", "Pieczywo", "Produkty sypkie", "Inne")
    val units = listOf("szt", "kg", "g", "l", "ml", "opak")

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Konfiguracja produktu") },
            text = {
                // Używamy Scrollable Column, aby na mniejszych ekranach formularz się zmieścił
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. BARCODE (na górze zgodnie z prośbą)
                    OutlinedTextField(
                        value = editedBarcode,
                        onValueChange = { editedBarcode = it },
                        label = { Text("Barcode") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 2. NAME
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Nazwa produktu") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 3. ID (nowe pole)
                    OutlinedTextField(
                        value = editedId,
                        onValueChange = { editedId = it },
                        label = { Text("ID Dokumentu (opcjonalnie)") },
                        placeholder = { Text("Zostaw puste dla auto-ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Kategoria:", style = MaterialTheme.typography.labelSmall)
                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }

                    Text("Jednostka:", style = MaterialTheme.typography.labelSmall)
                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                        units.forEach { u ->
                            FilterChip(
                                selected = selectedUnit == u,
                                onClick = { selectedUnit = u },
                                label = { Text(u) },
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val finalProduct = Product(
                        id = editedId.ifBlank { "" }, // Jeśli puste, repozytorium wygeneruje ID
                        name = editedName,
                        category = selectedCategory,
                        unit = selectedUnit,
                        barcodes = listOf(editedBarcode)
                    )
                    onApprove(request.id, finalProduct)
                    showEditDialog = false
                }) { Text("Dodaj do bazy") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Anuluj") }
            }
        )
    }

    // Widok karty na liście
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(request.name, style = MaterialTheme.typography.titleMedium)
                Text("Kod: ${request.barcode}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            // Czerwony X do odrzucenia
            IconButton(onClick = { onReject(request.id) }) {
                Icon(Icons.Default.Close, contentDescription = "Odrzuć", tint = Color.Red)
            }

            Button(onClick = { showEditDialog = true }) {
                Text("Zatwierdź")
            }
        }
    }
}