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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import com.example.posilkuz.data.model.Product
import com.example.posilkuz.data.repository.ProductRepository
import com.example.posilkuz.ui.components.AppSnackbar
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
    innerPadding: PaddingValues = PaddingValues(),
    onTestNotification: () -> Unit = {} // Nowy parametr dla testowego powiadomienia
) {
    var currentSubScreen by remember { mutableStateOf(ProfileSubScreen.MAIN) }

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
                onNavigateToAdmin = { currentSubScreen = ProfileSubScreen.ADMIN_PANEL }
            )
            ProfileSubScreen.ADMIN_PANEL -> AdminRequestsView(
                onBack = { currentSubScreen = ProfileSubScreen.MAIN }
            )
            ProfileSubScreen.SETTINGS -> SettingsView(
                onBack = { currentSubScreen = ProfileSubScreen.MAIN },
                onNavigateToDisplay = { currentSubScreen = ProfileSubScreen.DISPLAY },
                onTestNotification = onTestNotification // Przekazujemy funkcję do widoku ustawień
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

    ListItem(
        headlineContent = { Text("Zatwierdź produkty") },
        leadingContent = { Icon(Icons.Default.FactCheck, contentDescription = null) },
        trailingContent = { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null) },
        modifier = Modifier.clickable { onNavigateToAdmin() }
    )
}

@Composable
fun SettingsView(
    onBack: () -> Unit,
    onNavigateToDisplay: () -> Unit,
    onTestNotification: () -> Unit // Odbieramy funkcję powiadomienia
) {
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

    // Element listy odpalający testowe powiadomienie
    ListItem(
        headlineContent = { Text("Powiadomienie testowe") },
        supportingContent = { Text("Sprawdź, czy powiadomienia działają") },
        leadingContent = { Icon(Icons.Default.NotificationsActive, contentDescription = "Ikona dzwonka") },
        modifier = Modifier.clickable { onTestNotification() }
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
    var allProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        allProducts = repository.getAllProducts()

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
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                AppSnackbar(data)
            }
        },
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Wstecz") }
                Text("Zgłoszone produkty", style = MaterialTheme.typography.headlineSmall)
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(horizontal = 16.dp)) {

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
                            existingProducts = allProducts,
                            onApprove = { requestId, finalProduct ->
                                scope.launch {
                                    try {
                                        repository.approveProductRequest(requestId, finalProduct)
                                        snackbarHostState.showSnackbar("Dodano nowy produkt: ${finalProduct.name}")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Błąd podczas dodawania")
                                    }
                                }
                            },
                            onReject = { requestId ->
                                scope.launch {
                                    repository.rejectProductRequest(requestId)
                                    snackbarHostState.showSnackbar("Odrzucono zgłoszenie")
                                }
                            },
                            onAssignToExisting = { requestId, productId, barcode ->
                                scope.launch {
                                    try {
                                        repository.addBarcodeToExistingProduct(productId, barcode)
                                        repository.rejectProductRequest(requestId)
                                        snackbarHostState.showSnackbar("Przypisano kod do istniejącego produktu")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Błąd podczas przypisywania")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRequestCard(
    request: ProductRequest,
    existingProducts: List<Product>,
    onApprove: (requestId: String, finalProduct: Product) -> Unit,
    onReject: (String) -> Unit,
    onAssignToExisting: (requestId: String, productId: String, barcode: String) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showAssignDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var editedBarcode by remember { mutableStateOf(request.barcode) }
    var editedName by remember { mutableStateOf(request.name) }
    var editedId by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Inne") }
    var selectedUnit by remember { mutableStateOf("szt") }

    val categories = listOf("Warzywa", "Owoce", "Nabiał", "Mięso", "Napoje", "Pieczywo", "Produkty sypkie", "Inne")
    val units = listOf("szt", "kg", "g", "l", "ml", "opak")

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Konfiguracja produktu") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = editedBarcode,
                        onValueChange = { editedBarcode = it },
                        label = { Text("Barcode") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Nazwa produktu") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedId,
                        onValueChange = { editedId = it },
                        label = { Text("ID Dokumentu (opcjonalnie)") },
                        modifier = Modifier.fillMaxWidth()
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
                        id = editedId.ifBlank { "" },
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

    if (showAssignDialog) {
        AlertDialog(
            onDismissRequest = { showAssignDialog = false },
            title = { Text("Przypisz kod do produktu") },
            text = {
                Column(modifier = Modifier.heightIn(max = 400.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Szukaj produktu...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, null) }
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        val filtered = existingProducts.filter {
                            it.name.contains(searchQuery, ignoreCase = true)
                        }
                        items(filtered) { product ->
                            ListItem(
                                headlineContent = { Text(product.name) },
                                supportingContent = { Text("${product.category} • ${product.unit}") },
                                modifier = Modifier.clickable {
                                    onAssignToExisting(request.id, product.id, request.barcode)
                                    showAssignDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAssignDialog = false }) { Text("Anuluj") }
            }
        )
    }

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

            IconButton(onClick = { onReject(request.id) }) {
                Icon(Icons.Default.Close, contentDescription = "Odrzuć", tint = Color.Red)
            }

            IconButton(onClick = { showAssignDialog = true }) {
                Icon(Icons.Default.Link, contentDescription = "Przypisz", tint = MaterialTheme.colorScheme.primary)
            }

            Button(onClick = { showEditDialog = true }) {
                Text("Zatwierdź")
            }
        }
    }
}