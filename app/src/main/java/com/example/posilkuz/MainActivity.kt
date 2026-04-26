package com.example.posilkuz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.posilkuz.ui.RandomRecipe.RandomRecipeScreen
import com.example.posilkuz.ui.auth.AuthScreen
import com.example.posilkuz.ui.home.HomeScreen
import com.example.posilkuz.ui.navigation.openGroceryMaps
import com.example.posilkuz.ui.pantry.PantryScreen
import com.example.posilkuz.ui.profile.ProfileScreen
import com.example.posilkuz.ui.recipe.RecipesScreen
import com.example.posilkuz.ui.theme.PosilkUZTheme
import com.example.posilkuz.ui.theme.ThemeMode
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

data class TabItem(
    val label: String,
    val icon: ImageVector
)

// 4 zakładki — Sklepy dostępne jako kafelek na HomeScreen
val mainTabs = listOf(
    TabItem("Główna", Icons.Default.Home),
    TabItem("Spiżarnia", Icons.Default.ShoppingCart),
    TabItem("Przepisy", Icons.Default.Restaurant),
    TabItem("Profil", Icons.Default.Person)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            // Stan motywu z commitu kolegi — hoistowany tutaj, żeby działał globalnie
            var currentTheme by remember { mutableStateOf(ThemeMode.SYSTEM) }

            val navController = rememberNavController()
            val auth = FirebaseAuth.getInstance()
            val startDestination = if (auth.currentUser != null) "main" else "auth"
            val context = LocalContext.current
            val onShowMaps: () -> Unit = { context.openGroceryMaps() }

            PosilkUZTheme(themeMode = currentTheme) {
                // Surface z commitu kolegi — naprawia tło przy zmianie motywu
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("auth") {
                            AuthScreen(onAuthSuccess = {
                                navController.navigate("main") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            })
                        }

                        composable("main") {
                            MainPagerScreen(
                                currentTheme = currentTheme,
                                onThemeChange = { currentTheme = it },
                                onLogout = {
                                    auth.signOut()
                                    navController.navigate("auth") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                },
                                onShowMaps = onShowMaps,
                                        onNavigateToRandom = { navController.navigate("random_recipe") }  // ← DODAJ
                            )
                        }

                        composable("random_recipe") {  // ← DODAJ
                            RandomRecipeScreen(onBack = { navController.popBackStack() })
                        }

                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPagerScreen(
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    onLogout: () -> Unit,
    onShowMaps: () -> Unit,
    onNavigateToRandom: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { mainTabs.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            NavigationBar {
                mainTabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(
                                    page = index,
                                    animationSpec = tween(durationMillis = 350)
                                )
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true,
            beyondViewportPageCount = 1
        ) { page ->
            when (page) {
                0 -> HomeScreen(
                    onLogout = onLogout,
                    onNavigateToPantry = {
                        coroutineScope.launch { pagerState.animateScrollToPage(1) }
                    },
                    onNavigateToRecipes = {
                        coroutineScope.launch { pagerState.animateScrollToPage(2) }
                    },
                    onNavigateToProfile = {
                        coroutineScope.launch { pagerState.animateScrollToPage(3) }
                    },
                    onShowMaps = onShowMaps,
                    innerPadding = innerPadding
                )
                1 -> PantryScreen(innerPadding = innerPadding)
                2 -> RecipesScreen(
                    innerPadding = innerPadding,
                    onRandomClick = onNavigateToRandom
                )
                3 -> ProfileScreen(
                    currentTheme = currentTheme,
                    onThemeChange = onThemeChange,
                    innerPadding = innerPadding
                )
            }
        }
    }
}