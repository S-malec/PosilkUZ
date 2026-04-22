package com.example.posilkuz.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onNavigateToPantry: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onShowMaps: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var nickname by remember { mutableStateOf("...") }

    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    nickname = document.getString("nickname") ?: "Użytkowniku"
                }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                // Definiujemy elementy menu wraz z ich trasami/akcjami
                val items = listOf(
                    Triple("Główna", Icons.Default.Home, {}),
                    Triple("Spiżarnia", Icons.Default.ShoppingCart, onNavigateToPantry), // Tu przypisujemy akcję
                    Triple("Przepisy", Icons.Default.Restaurant, onNavigateToRecipes),
                    Triple("Sklepy", Icons.Default.ShoppingBasket, onShowMaps),
                    Triple("Profil", Icons.Default.Person, onNavigateToProfile)
                )

                items.forEach { (label, icon, action) ->
                    val isSelected = label == "Główna" // Logika zaznaczenia (na razie uproszczona)

                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = isSelected,
                        onClick = action // Wywołanie przekazanej akcji
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Nagłówek
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Witaj, $nickname!",
                    style = MaterialTheme.typography.headlineMedium
                )
                IconButton(onClick = onLogout) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Wyloguj")
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "W trakcie budowy!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                    Text(
                        text = "Wkrótce pojawią się tu Twoje posiłki!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    // Opcjonalnie: Przycisk skrótu na środku ekranu
                    Button(
                        onClick = onNavigateToPantry,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Zarządzaj składnikami")
                    }

                    Button(
                        onClick = onNavigateToRecipes
                    ) {
                        Text("Zarządzaj przepisami")
                    }
                }
            }
        }
    }
}