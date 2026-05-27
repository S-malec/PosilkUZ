package com.example.posilkuz.ui.pantry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.posilkuz.R
import com.example.posilkuz.data.model.Product
import com.example.posilkuz.data.normalizePolish
import com.example.posilkuz.ui.components.AppSnackbar
import com.example.posilkuz.ui.translation.TranslationHelper
import com.example.posilkuz.ui.translation.getDynamicString
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
    viewModel: PantryViewModel = viewModel(),
    innerPadding: PaddingValues = PaddingValues()
) {
    val products by viewModel.allProducts.collectAsState()
    val pantryIds by viewModel.userPantryIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    var showBarcodeDialog by remember { mutableStateOf(false) }
    val unrecognizedBarcode by viewModel.unrecognizedBarcode.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Nowe dynamiczne grupowanie, tłumaczenie i sortowanie z uwzględnieniem strings.xml
    val groupedProducts = remember(products, searchQuery, sortOrder) {
        val normalizedQuery = searchQuery.normalizePolish()

        val filtered = products.filter { product ->
            val localizedName = context.getDynamicString("prod", product.id, product.name)
            localizedName.normalizePolish().contains(normalizedQuery, ignoreCase = true)
        }

        val sorted = when (sortOrder) {
            PantryViewModel.SortOrder.ASCENDING -> filtered.sortedBy { context.getDynamicString("prod", it.id, it.name).normalizePolish() }
            PantryViewModel.SortOrder.DESCENDING -> filtered.sortedByDescending { context.getDynamicString("prod", it.id, it.name).normalizePolish() }
        }

        sorted.groupBy { context.getDynamicString("cat", it.category, it.category) }
    }

    unrecognizedBarcode?.let { barcode ->
        NewProductRequestDialog(
            barcode = barcode,
            onDismiss = { viewModel.closeRequestDialog() },
            onSubmit = { name, bCode ->
                viewModel.requestNewProduct(name, bCode)
                scope.launch {
                    snackbarHostState.showSnackbar(TranslationHelper.StringResource(R.string.add_request_sent).asString(context) + name)
                }
            }
        )
    }

    if (showBarcodeDialog) {
        BarcodeScannerDialog(
            onDismiss = { showBarcodeDialog = false },
            onBarcodeScanned = { barcode ->
                showBarcodeDialog = false

                scope.launch {
                    try {
                        val result = viewModel.addProductByBarcode(barcode)

                        when (result) {
                            PantryViewModel.AddProductResult.SUCCESS -> {
                                snackbarHostState.showSnackbar(TranslationHelper.StringResource(R.string.product_added_to_pantry).asString(context))
                            }
                            PantryViewModel.AddProductResult.NOT_FOUND -> {
                                // Formularz otworzy się automatycznie przez LiveData/Flow
                            }
                            PantryViewModel.AddProductResult.ERROR -> {
                                snackbarHostState.showSnackbar(TranslationHelper.StringResource(R.string.failed_to_add_product).asString(context))
                            }
                            else -> {} // Ta linijka naprawia błąd "must be exhaustive"
                        }
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar(TranslationHelper.StringResource(R.string.unexpected_error).asString(context))
                    }
                }
            }
        )
    }

    Box(modifier = Modifier.padding(innerPadding)) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    AppSnackbar(data)
                }
            },
            topBar = {
                Surface(tonalElevation = 3.dp) {
                    Column {
                        CenterAlignedTopAppBar(title = { Text(text = stringResource(R.string.your_pantry)) })

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.onSearchQueryChange(it) },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text(text = stringResource(R.string.search_product)) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear))
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            FilledTonalIconButton(
                                onClick = { viewModel.toggleSortOrder() },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SortByAlpha,
                                    contentDescription = stringResource(R.string.sort),
                                    modifier = Modifier.graphicsLayer {
                                        rotationX = if (sortOrder == PantryViewModel.SortOrder.DESCENDING) 180f else 0f
                                    }
                                )
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showBarcodeDialog = true },
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                    text = { Text(text = stringResource(R.string.scan)) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            floatingActionButtonPosition = FabPosition.End
        ) { scaffoldPadding ->
            if (isLoading) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(scaffoldPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(scaffoldPadding),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp)
                ) {
                    if (groupedProducts.isEmpty() && searchQuery.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .padding(bottom = 100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = stringResource(R.string.no_results_for) + searchQuery, color = Color.Gray)
                            }
                        }
                    }

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
                }
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
    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "rotation")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(CardDefaults.shape)
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
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
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer { rotationZ = rotationState }
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(bottom = 12.dp, start = 8.dp, end = 8.dp)
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
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onCheckedChange(!isSelected) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = LocalContext.current.getDynamicString("prod", product.id, product.name),
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.unit_label) + " " + product.unit,
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

@Composable
fun NewProductRequestDialog(
    barcode: String,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var productName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.unknown_product)) },
        text = {
            Column {
                Text(stringResource(R.string.product_not_found, barcode))
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text(stringResource(R.string.product_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = productName.isNotBlank(),
                onClick = { onSubmit(productName, barcode) }
            ) { Text(stringResource(R.string.send_request)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}