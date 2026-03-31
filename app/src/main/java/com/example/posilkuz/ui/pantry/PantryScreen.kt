package com.example.posilkuz.ui.pantry

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // KLUCZOWY IMPORT
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant // IMPORT DLA IKONY
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.posilkuz.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
    viewModel: PantryViewModel = viewModel(),
    onNavigateToHome: () -> Unit,      // Dodana akcja powrotu
    onNavigateToRecipes: () -> Unit,   // To już masz (przycisk FAB)
    onNavigateToProfile: () -> Unit    // Dodana akcja profilu
) {
    val products by viewModel.allProducts.collectAsState()
    val pantryIds by viewModel.userPantryIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Twoja Spiżarnia") }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToRecipes,
                icon = { Icon(Icons.Default.Restaurant, contentDescription = null) },
                text = { Text("Co mogę zjeść?") }
            )
        },
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    Triple("Główna", Icons.Default.Home, onNavigateToHome), // <--- TA FUNKCJA MUSI BYĆ PRZEKAZANA
                    Triple("Spiżarnia", Icons.Default.ShoppingCart, {}),
                    Triple("Sklepy", Icons.Default.Star, {}),
                    Triple("Profil", Icons.Default.Person, onNavigateToProfile)
                )

                items.forEach { (label, icon, action) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = label == "Spiżarnia",
                        onClick = {
                            // Bezpośrednie wywołanie action() zapewnia, że lambda zostanie wykonana
                            action()
                        }
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(products) { product ->
                    ProductRow(
                        product = product,
                        isSelected = pantryIds.contains(product.id),
                        onCheckedChange = { viewModel.toggleProduct(product.id) }
                    )
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isSelected) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Checkbox(
                checked = isSelected,
                onCheckedChange = onCheckedChange
            )
        }
    }
}




