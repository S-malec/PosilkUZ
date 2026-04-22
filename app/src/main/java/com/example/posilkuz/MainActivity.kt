package com.example.posilkuz

import android.os.Bundle
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.posilkuz.ui.auth.AuthScreen
import com.example.posilkuz.ui.home.HomeScreen
import com.example.posilkuz.ui.pantry.PantryScreen
import com.example.posilkuz.ui.recipe.RecipesScreen
import com.example.posilkuz.ui.theme.PosilkUZTheme
import com.google.firebase.auth.FirebaseAuth
import com.example.posilkuz.ui.navigation.openGroceryMaps

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ... (twoja inicjalizacja Firebase)

        enableEdgeToEdge()
        setContent {
            PosilkUZTheme {
                val navController = rememberNavController()
                val auth = FirebaseAuth.getInstance()

                // Sprawdzamy czy użytkownik jest zalogowany na starcie
                val startDestination = if (auth.currentUser != null) "home" else "auth"

                val context = LocalContext.current
                val onShowMaps: () -> Unit = { context.openGroceryMaps() }

                NavHost(navController = navController, startDestination = startDestination) {
                    // Ekran logowania
                    composable("auth") {
                        AuthScreen(onAuthSuccess = {
                            navController.navigate("home") {
                                popUpTo("auth") { inclusive = true } // Czyścimy historię, żeby nie wracać do logowania
                            }
                        })
                    }

                    composable("home") {
                        HomeScreen(
                            onLogout = {
                                auth.signOut()
                                navController.navigate("auth") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onNavigateToPantry = { navigateToTab(navController, "pantry") },
                            onNavigateToRecipes = { navigateToTab(navController, "recipes") },
                            onNavigateToProfile = { /* navigateToTab(navController, "profile") */ },
                            onShowMaps = onShowMaps
                        )
                    }

                    composable("pantry") {
                        PantryScreen(
                            onNavigateToHome = { navigateToTab(navController, "home") },
                            onNavigateToRecipes = { navigateToTab(navController, "recipes") },
                            onNavigateToProfile = { /* navigateToTab(navController, "profile") */ },
                            onShowMaps = onShowMaps
                        )
                    }

                    composable("recipes") {
                        RecipesScreen(
                            onNavigateToHome = { navigateToTab(navController, "home") },
                            onNavigateToPantry = { navigateToTab(navController, "pantry") },
                            onNavigateToProfile = { /* navigateToTab(navController, "profile") */ },
                            onShowMaps = onShowMaps
                        )
                    }
                }
            }
        }
    }

    fun navigateToTab(navController: NavHostController, route: String) {
        navController.navigate(route) {
            // Cofa do startu przed wejściem na nową zakładkę, żeby nie budować stosu
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}