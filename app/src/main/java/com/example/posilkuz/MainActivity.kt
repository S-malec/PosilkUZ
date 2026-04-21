package com.example.posilkuz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.posilkuz.data.importRecipesFromJson
import com.example.posilkuz.ui.auth.AuthScreen
import com.example.posilkuz.ui.home.HomeScreen
import com.example.posilkuz.ui.pantry.PantryScreen
import com.example.posilkuz.ui.recipe.RecipesScreen
import com.example.posilkuz.ui.theme.PosilkUZTheme

import com.example.posilkuz.ui.profile.ProfileScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.posilkuz.ui.theme.ThemeMode
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme

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
            var currentTheme by remember { mutableStateOf(ThemeMode.SYSTEM) }
            val navController = rememberNavController()
            val auth = FirebaseAuth.getInstance()

            PosilkUZTheme(themeMode = currentTheme) {
                // TO JEST KLUCZ DO NAPRAWY TŁA: Surface wypełnia ekran kolorem z motywu!
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val startDestination = if (auth.currentUser != null) "home" else "auth"

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("auth") {
                            AuthScreen(onAuthSuccess = {
                                navController.navigate("home") {
                                    popUpTo("auth") { inclusive = true }
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
                                onNavigateToProfile = { navigateToTab(navController, "profile") }
                            )
                        }

                        composable("pantry") {
                            PantryScreen(
                                onNavigateToHome = { navigateToTab(navController, "home") },
                                onNavigateToRecipes = { navigateToTab(navController, "recipes") },
                                onNavigateToProfile = { navigateToTab(navController, "profile") }
                            )
                        }

                        composable("recipes") {
                            RecipesScreen(
                                onNavigateToHome = { navigateToTab(navController, "home") },
                                onNavigateToPantry = { navigateToTab(navController, "pantry") },
                                onNavigateToProfile = { navigateToTab(navController, "profile") }
                            )
                        }

                        composable("profile") {
                            ProfileScreen(
                                currentTheme = currentTheme,
                                onThemeChange = { newTheme -> currentTheme = newTheme },
                                onNavigateToHome = { navigateToTab(navController, "home") },
                                onNavigateToPantry = { navigateToTab(navController, "pantry") },
                                onNavigateToRecipes = { navigateToTab(navController, "recipes") }
                            )
                        }
                    }
                }
            }
        }
    } // <-- TUTEJ DODAŁEM BRAKUJĄCĄ KLAMRĘ ZAMYKAJĄCĄ onCreate!

    fun navigateToTab(navController: NavHostController, route: String) {
        navController.navigate(route) {
            // Cofa do startu przed wejściem na nową zakładkę, żeby nie budować stosu
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}