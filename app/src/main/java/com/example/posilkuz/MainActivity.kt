package com.example.posilkuz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.posilkuz.ui.auth.AuthScreen
import com.example.posilkuz.ui.home.HomeScreen
import com.example.posilkuz.ui.pantry.PantryScreen
import com.example.posilkuz.ui.theme.PosilkUZTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth

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

                NavHost(navController = navController, startDestination = startDestination) {
                    // Ekran logowania
                    composable("auth") {
                        AuthScreen(onAuthSuccess = {
                            navController.navigate("home") {
                                popUpTo("auth") { inclusive = true } // Czyścimy historię, żeby nie wracać do logowania
                            }
                        })
                    }

                    // Ekran główny (z dolnym menu)
                    composable("home") {
                        HomeScreen(
                            onLogout = {
                                auth.signOut()
                                navController.navigate("auth") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onNavigateToPantry = {
                                navController.navigate("pantry")
                            },
                            onNavigateToProfile = {
                                // Pusta lambda rozwiązuje błąd "No value passed for parameter"
                            }
                        )
                    }

                    // W MainActivity.kt
                    composable("pantry") {
                        PantryScreen(
                            onNavigateToHome = {
                                // Uproszczona wersja bez parametrów dodatkowych na start
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onNavigateToRecipes = {
                                navController.navigate("recipes")
                            },
                            onNavigateToProfile = { }
                        )
                    }

                    // Ekran wyników (przepisów)
                    composable("recipes") {
                        // RecipeListScreen() - tu dodasz widok przepisów
                    }
                }
            }
        }
    }
}