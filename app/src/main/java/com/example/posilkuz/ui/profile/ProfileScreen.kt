package com.example.posilkuz.ui.profile

import android.app.Activity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.example.posilkuz.R
import com.example.posilkuz.data.model.Product
import com.example.posilkuz.data.repository.ProductRepository
import com.example.posilkuz.ui.components.AppSnackbar
import com.example.posilkuz.ui.translation.LanguageHelper
import com.example.posilkuz.ui.translation.TranslationHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

/**
 * Wyliczenie reprezentujące możliwe podekrany w sekcji profilu użytkownika.
 */
enum class ProfileSubScreen {
    /** Widok główny profilu z listą opcji. */
    MAIN,
    /** Widok ustawień aplikacji. */
    SETTINGS,
    /** Widok ustawień wyglądu (motyw). */
    DISPLAY,
    /** Widok ustawień języka aplikacji. */
    LANGUAGE,
    /** Panel administratora do zarządzania zgłoszeniami produktów. */
    ADMIN_PANEL
}

/**
 * Ekran profilu użytkownika zarządzający nawigacją między podekranami.
 *
 * Obsługuje przycisk Wstecz systemu Android, cofając do poprzedniego podekranu
 * zamiast zamykać aplikację.
 *
 * @param currentTheme aktualnie wybrany motyw aplikacji
 * @param onThemeChange wywołanie zwrotne zmiany motywu przez użytkownika
 * @param innerPadding padding wewnętrzny przekazywany z zewnętrznego [Scaffold]
 */
@Composable
fun ProfileScreen(
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    innerPadding: PaddingValues = PaddingValues()
) {
    var currentSubScreen by remember { mutableStateOf(ProfileSubScreen.MAIN) }

    BackHandler(enabled = currentSubScreen != ProfileSubScreen.MAIN) {
        currentSubScreen = when (currentSubScreen) {
            ProfileSubScreen.DISPLAY -> ProfileSubScreen.SETTINGS
            ProfileSubScreen.LANGUAGE -> ProfileSubScreen.SETTINGS
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
                onNavigateToLanguage = { currentSubScreen = ProfileSubScreen.LANGUAGE }
            )
            ProfileSubScreen.DISPLAY -> DisplaySettingsView(
                currentTheme = currentTheme,
                onThemeChange = onThemeChange,
                onBack = { currentSubScreen = ProfileSubScreen.SETTINGS }
            )
            ProfileSubScreen.LANGUAGE -> LanguageSettingsView(
                onBack = { currentSubScreen = ProfileSubScreen.SETTINGS }
            )
        }
    }
}

/**
 * Główny widok profilu z listą dostępnych opcji.
 *
 * @param onNavigateToSettings wywołanie zwrotne nawigacji do podekranu ustawień
 * @param onNavigateToAdmin wywołanie zwrotne nawigacji do panelu administratora
 */
@Composable
fun ProfileMainView(onNavigateToSettings: () -> Unit, onNavigateToAdmin: () -> Unit) {
    Text(text = stringResource(R.string.my_profile),
        style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 24.dp))

    ListItem(
        headlineContent = { Text(text = stringResource(R.string.settings)) },
        leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) },
        trailingContent = { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null) },
        modifier = Modifier.clickable { onNavigateToSettings() }
    )

    ListItem(
        headlineContent = { Text(text = stringResource(R.string.approve_products)) },
        leadingContent = { Icon(Icons.Default.FactCheck, contentDescription = null) },
        trailingContent = { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null) },
        modifier = Modifier.clickable { onNavigateToAdmin() }
    )
}

/**
 * Podekran ustawień z listą kategorii ustawień.
 *
 * @param onBack wywołanie zwrotne powrotu do głównego widoku profilu
 * @param onNavigateToDisplay wywołanie zwrotne nawigacji do ustawień wyglądu
 * @param onNavigateToLanguage wywołanie zwrotne nawigacji do ustawień języka
 */
@Composable
fun SettingsView(
    onBack: () -> Unit,
    onNavigateToDisplay: () -> Unit,
    onNavigateToLanguage: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back)) }
        Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineSmall)
    }

    ListItem(
        headlineContent = { Text(stringResource(R.string.display)) },
        leadingContent = { Icon(Icons.Default.DarkMode, contentDescription = null) },
        trailingContent = { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null) },
        modifier = Modifier.clickable { onNavigateToDisplay() }
    )

    ListItem(
        headlineContent = { Text(stringResource(R.string.language)) },
        leadingContent = { Icon(Icons.Default.Language, contentDescription = null) },
        trailingContent = { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null) },
        modifier = Modifier.clickable { onNavigateToLanguage() }
    )
}

/**
 * Podekran ustawień wyglądu pozwalający wybrać motyw aplikacji.
 *
 * @param currentTheme aktualnie wybrany motyw
 * @param onThemeChange wywołanie zwrotne zmiany motywu
 * @param onBack wywołanie zwrotne powrotu do ekranu ustawień
 */
@Composable
fun DisplaySettingsView(
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back)) }
        Text(text = stringResource(R.string.display), style = MaterialTheme.typography.headlineSmall)
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            ThemeOptionRow(stringResource(R.string.theme_system), currentTheme == ThemeMode.SYSTEM) { onThemeChange(ThemeMode.SYSTEM) }
            ThemeOptionRow(stringResource(R.string.theme_light), currentTheme == ThemeMode.LIGHT) { onThemeChange(ThemeMode.LIGHT) }
            ThemeOptionRow(stringResource(R.string.theme_dark), currentTheme == ThemeMode.DARK) { onThemeChange(ThemeMode.DARK) }
        }
    }
}

/**
 * Podekran ustawień języka aplikacji.
 *
 * Zapisuje wybrany język w SharedPreferences i restartuje aktywność,
 * aby zastosować zmianę lokalizacji.
 *
 * @param onBack wywołanie zwrotne powrotu do ekranu ustawień
 */
@Composable
fun LanguageSettingsView(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity

    val languages = listOf(
        "system" to stringResource(R.string.language_system),
        "pl" to stringResource(R.string.language_polish),
        "en" to stringResource(R.string.language_english)
    )

    var selectedLang by remember {
        mutableStateOf(LanguageHelper.getSavedLanguage(context))
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back)) }
        Text(stringResource(R.string.language), style = MaterialTheme.typography.headlineSmall)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            languages.forEach { (tag, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedLang = tag
                            LanguageHelper.saveLanguage(context, tag)
                            activity?.recreate()
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selectedLang == tag, onClick = null)
                    Spacer(Modifier.width(12.dp))
                    Text(label)
                }
            }
        }
    }
}

/**
 * Wiersz opcji motywu z przyciskiem radiowym.
 *
 * @param text etykieta opcji motywu
 * @param selected `true`, jeśli ta opcja jest aktualnie wybrana
 * @param onClick wywołanie zwrotne po wybraniu opcji
 */
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

/**
 * Panel administratora wyświetlający oczekujące zgłoszenia nowych produktów.
 *
 * Nasłuchuje zmian w kolekcji `product_requests` w Firestore w czasie rzeczywistym.
 * Umożliwia zatwierdzenie zgłoszenia jako nowy produkt, przypisanie kodu kreskowego
 * do istniejącego produktu lub odrzucenie zgłoszenia.
 *
 * @param onBack wywołanie zwrotne powrotu do głównego widoku profilu
 * @param repository repozytorium produktów używane do operacji zatwierdzania i odrzucania
 */
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
    val context = LocalContext.current

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
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, stringResource(R.string.back)) }
                Text(text = stringResource(R.string.reported_products), style = MaterialTheme.typography.headlineSmall)
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
                    Text(text = stringResource(R.string.no_new_reports), color = Color.Gray)
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
                                        snackbarHostState.showSnackbar(TranslationHelper.StringResource(R.string.new_product_added).asString(context) + finalProduct.name)
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar(TranslationHelper.StringResource(R.string.error_adding).asString(context))
                                    }
                                }
                            },
                            onReject = { requestId ->
                                scope.launch {
                                    repository.rejectProductRequest(requestId)
                                    snackbarHostState.showSnackbar(TranslationHelper.StringResource(R.string.report_rejected).asString(context))
                                }
                            },
                            onAssignToExisting = { requestId, productId, barcode ->
                                scope.launch {
                                    try {
                                        repository.addBarcodeToExistingProduct(productId, barcode)
                                        repository.rejectProductRequest(requestId)
                                        snackbarHostState.showSnackbar(TranslationHelper.StringResource(R.string.code_assigned).asString(context))
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar(TranslationHelper.StringResource(R.string.error_assigning).asString(context))
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

/**
 * Karta pojedynczego zgłoszenia produktu w panelu administratora.
 *
 * Wyświetla nazwę i kod kreskowy zgłoszonego produktu oraz trzy przyciski akcji:
 * odrzucenia, przypisania do istniejącego produktu i zatwierdzenia jako nowy produkt.
 * Zatwierdzenie i przypisanie odbywają się przez dedykowane dialogi z formularzami.
 *
 * @param request zgłoszenie produktu do wyświetlenia
 * @param existingProducts lista wszystkich produktów w bazie danych (do wyszukiwania przy przypisaniu)
 * @param onApprove wywołanie zwrotne zatwierdzenia zgłoszenia z docelowym obiektem [Product]
 * @param onReject wywołanie zwrotne odrzucenia zgłoszenia z identyfikatorem zgłoszenia
 * @param onAssignToExisting wywołanie zwrotne przypisania kodu kreskowego do istniejącego produktu
 */
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

    val categories = stringArrayResource(R.array.product_categories).toList()
    val units = stringArrayResource(R.array.product_units).toList()

    var selectedCategory by remember { mutableStateOf(categories.last()) }
    var selectedUnit by remember { mutableStateOf(units.first()) }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(text = stringResource(R.string.edit_product)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = editedBarcode,
                        onValueChange = { editedBarcode = it },
                        label = { Text(text = stringResource(R.string.barcode)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text(text = stringResource(R.string.product_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedId,
                        onValueChange = { editedId = it },
                        label = { Text(text = stringResource(R.string.document_id_optional)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = stringResource(R.string.category_label), style = MaterialTheme.typography.labelSmall)
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

                    Text(text = stringResource(R.string.unit_label), style = MaterialTheme.typography.labelSmall)
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
                }) { Text(text = stringResource(R.string.add_to_database)) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text(text = stringResource(R.string.cancel)) }
            }
        )
    }

    if (showAssignDialog) {
        AlertDialog(
            onDismissRequest = { showAssignDialog = false },
            title = { Text(text = stringResource(R.string.assign_code_to_product)) },
            text = {
                Column(modifier = Modifier.heightIn(max = 400.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text(text = stringResource(R.string.search_product_dots)) },
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
                TextButton(onClick = { showAssignDialog = false }) { Text(text = stringResource(R.string.cancel)) }
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
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.reject), tint = Color.Red)
            }

            IconButton(onClick = { showAssignDialog = true }) {
                Icon(Icons.Default.Link, contentDescription = stringResource(R.string.assign), tint = MaterialTheme.colorScheme.primary)
            }

            Button(onClick = { showEditDialog = true }) {
                Text(text = stringResource(R.string.confirm))
            }
        }
    }
}
