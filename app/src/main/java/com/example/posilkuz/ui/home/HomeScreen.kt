package com.example.posilkuz.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.posilkuz.data.model.Recipe
import com.example.posilkuz.data.repository.PinnedRecipeRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onNavigateToPantry: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onShowMaps: () -> Unit,
    innerPadding: PaddingValues = PaddingValues()
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var nickname by remember { mutableStateOf("...") }

    val pinnedRecipe by PinnedRecipeRepository.pinnedRecipe.collectAsState()

    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    nickname = document.getString("nickname") ?: "Użytkowniku"
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Cześć, $nickname! 👋",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Co dziś gotujesz?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onLogout) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Wyloguj")
            }
        }


        AnimatedVisibility(
            visible = pinnedRecipe != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            pinnedRecipe?.let { recipe ->
                PinnedRecipeCard(
                    recipe = recipe,
                    onUnpin = { PinnedRecipeRepository.unpin() }
                )
            }
        }


        MapsCard(onShowMaps = onShowMaps)


        Text(
            text = "Szybkie akcje",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.ShoppingCart,
                title = "Spiżarnia",
                subtitle = "Zarządzaj składnikami",
                onClick = onNavigateToPantry
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Restaurant,
                title = "Przepisy",
                subtitle = "Przeglądaj przepisy",
                onClick = onNavigateToRecipes
            )
        }
    }
}

@Composable
private fun PinnedRecipeCard(recipe: Recipe, onUnpin: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape)
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Przypięty przepis",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onUnpin,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Odepnij",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.graphicsLayer { rotationZ = rotationState },
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            AnimatedVisibility(visible = !isExpanded && recipe.ingredientIds.isNotEmpty()) {
                Text(
                    text = "${recipe.ingredientIds.size} składników",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "Składniki:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    recipe.ingredientIds.forEach { ingredientId ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = ingredientId.replace("_", " "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)
                    )

                    Text(
                        text = "Instrukcja:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = recipe.instructions,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MapsCard(onShowMaps: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape)
            .clickable { onShowMaps() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBasket,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Znajdź sklep spożywczy",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Otwórz Google Maps z najbliższymi sklepami",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(CardDefaults.shape)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}