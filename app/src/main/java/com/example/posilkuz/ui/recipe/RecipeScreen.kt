package com.example.posilkuz.ui.recipe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Receipt
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
import com.example.posilkuz.data.model.Recipe
import com.example.posilkuz.data.repository.PinnedRecipeRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    viewModel: RecipesViewModel = viewModel(),
    innerPadding: PaddingValues = PaddingValues(),
    onRandomClick: () -> Unit = {}
) {
    val recipes by viewModel.recipes.collectAsState()
    val pantryIds by viewModel.userPantryIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = stringResource(R.string.our_recipes)) })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onRandomClick,
                icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                text = { Text(text = stringResource(R.string.our_recipes)) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { scaffoldPadding ->
        if (isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
                    .padding(bottom = innerPadding.calculateBottomPadding()),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
                    .padding(
                        bottom = innerPadding.calculateBottomPadding(),
                        start = 16.dp,
                        end = 16.dp
                    )
            ) {
                items(recipes) { recipe ->
                    RecipeCard(recipe = recipe, userPantryIds = pantryIds)
                }
            }
        }
    }
}

@Composable
fun RecipeCard(
    recipe: Recipe,
    userPantryIds: Set<String>,
    onPinToggle: ((Recipe) -> Unit)? = null
) {
    val context = LocalContext.current

    var isExpanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    val pinnedRecipe by PinnedRecipeRepository.pinnedRecipe.collectAsState()
    val isPinned = pinnedRecipe?.title == recipe.title

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(CardDefaults.shape)
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = if (isPinned)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )

                // Przycisk przypięcia
                IconButton(
                    onClick = {
                        PinnedRecipeRepository.toggle(context,recipe)
                        onPinToggle?.invoke(recipe)
                    }
                ) {
                    Icon(
                        imageVector = if (isPinned) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isPinned) stringResource(R.string.unpin_recipe) else stringResource(R.string.pin_to_home),
                        tint = if (isPinned)
                            MaterialTheme.colorScheme.secondary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer { rotationZ = rotationState }
                )
            }

            if (isPinned) {
                Text(
                    text = stringResource(R.string.pinned_on_home),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = stringResource(R.string.ingredients_colon),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    recipe.ingredientIds.forEach { ingredientId ->
                        val hasIngredient = userPantryIds.contains(ingredientId)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (hasIngredient) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (hasIngredient) Color(0xFF4CAF50) else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = ingredientId.replace("_", " "),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (hasIngredient) MaterialTheme.colorScheme.onSurface else Color.Gray
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(
                        text = stringResource(R.string.instruction_colon),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = recipe.instructions,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}