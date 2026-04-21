package com.example.posilkuz.ui.pantry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // KLUCZOWY IMPORT
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restaurant // IMPORT DLA IKONY
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.posilkuz.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
    viewModel: PantryViewModel = viewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val groupedProducts by viewModel.groupedProducts.collectAsState(initial = emptyMap())
    val pantryIds by viewModel.userPantryIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    var showBarcodeDialog by remember { mutableStateOf(false) }

    if (showBarcodeDialog) {
        BarcodeScannerDialog(
            onDismiss = { showBarcodeDialog = false },
            onBarcodeScanned = { barcode -> viewModel.addProductByBarcode(barcode) }
        )
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(title = { Text("Twoja Spiżarnia") })

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Wyszukiwarka
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier.weight(1f), // Zajmuje dostępną przestrzeń
                        placeholder = { Text("Szukaj produktu...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Wyczyść")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Przycisk sortowania
                    FilledTonalIconButton(
                        onClick = { viewModel.toggleSortOrder() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(56.dp) // Wysokość taka sama jak TextField
                    ) {
                        Icon(
                            imageVector = if (sortOrder == PantryViewModel.SortOrder.ASCENDING)
                                Icons.Default.SortByAlpha
                            else
                                Icons.Default.SortByAlpha,
                            contentDescription = "Sortuj",
                            modifier = Modifier.graphicsLayer {
                                rotationX = if (sortOrder == PantryViewModel.SortOrder.DESCENDING) 180f else 0f
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showBarcodeDialog = true }, // Otwiera dialog
                icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                text = { Text("Skanuj") }
            )
        },
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    Triple("Główna", Icons.Default.Home, onNavigateToHome),
                    Triple("Spiżarnia", Icons.Default.ShoppingCart, {}),
                    Triple("Przepisy", Icons.Default.Restaurant, onNavigateToRecipes),
                    Triple("Sklepy", Icons.Default.Star, {}),
                    Triple("Profil", Icons.Default.Person, onNavigateToProfile)
                )

                items.forEach { (label, icon, action) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = label == "Spiżarnia",
                        onClick = { action() }
                    )
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // LazyColumn teraz uwzględnia padding z Scaffolda
            LazyColumn(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp) // Dodatkowy padding dla estetyki kafelków
            ) {
                if (groupedProducts.isEmpty() && searchQuery.isNotEmpty()) {
                    item {
                        Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Brak wyników dla: \"$searchQuery\"", color = Color.Gray)
                        }
                    }
                }
                // Iterujemy po mapie grup produktowych
                groupedProducts.forEach { (category, productsInCategory) ->
                    item {
                        CategorySection(
                            categoryName = category,
                            products = productsInCategory,
                            pantryIds = pantryIds,
                            onProductToggle = { id -> viewModel.toggleProduct(id) }
                        )
                    }
                }

                // Dodajemy pusty element na dole, żeby FAB nie zasłaniał ostatniego produktu
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun CategorySection(
    categoryName: String,
    products: List<Product>,
    pantryIds: Set<String>,
    onProductToggle: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Animacja obrotu ikonki strzałki
    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(CardDefaults.shape) // Dzięki temu ripple będzie miał kształt karty, nie okręgu
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            // Delikatna zmiana koloru gdy rozwinięte
            containerColor = if (isExpanded)
                MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore, // Używamy jednej ikony i ją obracamy
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer { rotationZ = rotationState }
                )
            }

            // Płynne rozwijanie listy
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .padding(bottom = 12.dp, start = 8.dp, end = 8.dp)
                ) {
                    products.forEach { product ->
                        ProductRow(
                            product = product,
                            isSelected = pantryIds.contains(product.id),
                            onCheckedChange = { onProductToggle(product.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductRow(
    product: Product,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    // Animacja koloru tła dla zaznaczonego produktu
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else Color.Transparent,
        label = "bgColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp)) // Zaokrąglamy rogi podświetlenia i tła
            .background(backgroundColor)
            .clickable { onCheckedChange(!isSelected) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Jednostka: ${product.unit}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        Checkbox(
            checked = isSelected,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}




