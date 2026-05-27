package com.example.posilkuz

import android.R.attr.icon
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings.Global.getString
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.posilkuz.data.repository.PinnedRecipeRepository
import com.example.posilkuz.ui.RandomRecipe.RandomRecipeScreen
import com.example.posilkuz.ui.auth.AuthScreen
import com.example.posilkuz.ui.home.HomeScreen
import com.example.posilkuz.ui.navigation.openGroceryMaps
import com.example.posilkuz.ui.pantry.PantryScreen
import com.example.posilkuz.ui.profile.ProfileScreen
import com.example.posilkuz.ui.recipe.RecipesScreen
import com.example.posilkuz.ui.theme.PosilkUZTheme
import com.example.posilkuz.ui.theme.ThemeMode
import com.example.posilkuz.ui.translation.LanguageHelper
import com.example.posilkuz.utils.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Model danych reprezentujący pojedynczą zakładkę w dolnym pasku nawigacji.
 *
 * @property label tekst etykiety wyświetlanej pod ikoną zakładki
 * @property icon ikona wektorowa wyświetlana w zakładce
 */
data class TabItem(
    val label: String,
    val icon: ImageVector
)

/**
 * Główna aktywność aplikacji PosiłkUZ.
 *
 * Odpowiada za inicjalizację nawigacji, ustawienie języka aplikacji
 * oraz zarządzanie stanem motywu. Konfiguruje graf nawigacyjny złożony
 * z ekranu autoryzacji ([AuthScreen]) i ekranu głównego ([MainPagerScreen]).
 */
class MainActivity : ComponentActivity() {

    /**
     * Nadpisuje kontekst bazowy w celu zastosowania zapisanego języka użytkownika
     * przed utworzeniem aktywności.
     *
     * @param newBase bazowy kontekst przekazany przez system Android
     */
    override fun attachBaseContext(newBase: Context) {
        val lang = LanguageHelper.getSavedLanguage(newBase)
        val context = LanguageHelper.setLocale(newBase, lang)
        super.attachBaseContext(context)
    }

    /**
     * Tworzy aktywność, konfiguruje zakładki nawigacyjne, inicjalizuje [PinnedRecipeRepository]
     * oraz buduje drzewo UI z Jetpack Compose.
     *
     * @param savedInstanceState stan zapisany z poprzedniej instancji aktywności lub `null`
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 4 zakładki — Sklepy dostępne jako kafelek na HomeScreen
        val mainTabs = listOf(
            TabItem(getString(R.string.nav_home), Icons.Default.Home),
            TabItem(getString(R.string.nav_pantry), Icons.Default.ShoppingCart),
            TabItem(getString(R.string.nav_recipes), Icons.Default.Restaurant),
            TabItem(getString(R.string.nav_profile), Icons.Default.Person)
        )

        PinnedRecipeRepository.initialize(this)

        enableEdgeToEdge()
        setContent {
            var currentTheme by remember { mutableStateOf(ThemeMode.SYSTEM) }

            val navController = rememberNavController()
            val auth = FirebaseAuth.getInstance()
            val startDestination = if (auth.currentUser != null) "main" else "auth"
            val context = LocalContext.current
            val onShowMaps: () -> Unit = { context.openGroceryMaps() }

            val notificationHelper = remember { NotificationHelper(context) }

            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    notificationHelper.sendTestNotification()
                }
            }

            val handleTestNotification = {
                if (Build.VERSION.SDK_INT >= 33) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, "android.permission.POST_NOTIFICATIONS"
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) {
                        notificationHelper.sendTestNotification()
                    } else {
                        permissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
                    }
                } else {
                    notificationHelper.sendTestNotification()
                }
            }

            val handleRecipeReminder = { recipeName: String ->
                if (Build.VERSION.SDK_INT >= 33) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, "android.permission.POST_NOTIFICATIONS"
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) {
                        notificationHelper.sendRecipeReminder(recipeName)
                    } else {
                        permissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
                    }
                } else {
                    notificationHelper.sendRecipeReminder(recipeName)
                }
            }

            PosilkUZTheme(themeMode = currentTheme) {
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
                                mainTabs = mainTabs,
                                currentTheme = currentTheme,
                                onThemeChange = { currentTheme = it },
                                onLogout = {
                                    auth.signOut()
                                    navController.navigate("auth") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                },
                                onShowMaps = onShowMaps,
                                onNavigateToRandom = { navController.navigate("random_recipe") },
                                onTestNotification = handleTestNotification,
                                onRecipeReminder = handleRecipeReminder
                            )
                        }

                        composable("random_recipe") {
                            RandomRecipeScreen(onBack = { navController.popBackStack() })
                        }

                    }
                }
            }
        }
    }
}

/**
 * Ekran główny aplikacji oparty na poziomym pagerze ze czterema zakładkami.
 *
 * Renderuje dolny pasek nawigacji ([NavigationBar]) oraz przełącza strony pagera
 * z animacją po kliknięciu zakładki lub przeciągnięciu palcem.
 *
 * @param mainTabs lista zakładek do wyświetlenia w dolnym pasku nawigacji
 * @param currentTheme aktualnie wybrany motyw aplikacji
 * @param onThemeChange wywołanie zwrotne przy zmianie motywu przez użytkownika
 * @param onLogout wywołanie zwrotne wylogowania użytkownika
 * @param onShowMaps wywołanie zwrotne otwierające Google Maps z pobliskimi sklepami
 * @param onNavigateToRandom wywołanie zwrotne nawigacji do ekranu losowego przepisu
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPagerScreen(
    mainTabs: List<TabItem>,
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    onLogout: () -> Unit,
    onShowMaps: () -> Unit,
    onNavigateToRandom: () -> Unit,
    onTestNotification: () -> Unit,
    onRecipeReminder: (String) -> Unit
) {

    val pagerState = rememberPagerState(pageCount = { mainTabs.size })
    val coroutineScope = rememberCoroutineScope()

    val tabLabels = listOf(
        stringResource(R.string.nav_home),
        stringResource(R.string.nav_pantry),
        stringResource(R.string.nav_recipes),
        stringResource(R.string.nav_profile)
    )

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
                    onRecipeReminder = onRecipeReminder,
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
                    innerPadding = innerPadding,
                    onTestNotification = onTestNotification
                )
            }
        }
    }
}
