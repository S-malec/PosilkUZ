package com.example.posilkuz.data.repository

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.posilkuz.data.model.Recipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// Rozszerzenie dla Context, aby uzyskać dostęp do DataStore
private val Context.dataStore by preferencesDataStore(name = "pinned_recipe_prefs")

object PinnedRecipeRepository {
    private val _pinnedRecipe = MutableStateFlow<Recipe?>(null)
    val pinnedRecipe: StateFlow<Recipe?> = _pinnedRecipe.asStateFlow()

    // Klucze do zapisu danych
    private val TITLE_KEY = stringPreferencesKey("recipe_title")
    private val INSTRUCTIONS_KEY = stringPreferencesKey("recipe_instructions")
    private val INGREDIENTS_KEY = stringSetPreferencesKey("recipe_ingredients")

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Tę funkcję musisz wywołać raz, np. w MainActivity w onCreate,
     * aby wczytać zapisany przepis przy starcie.
     */
    fun initialize(context: Context) {
        scope.launch {
            val preferences = context.dataStore.data.first()
            val title = preferences[TITLE_KEY]
            if (title != null) {
                _pinnedRecipe.value = Recipe(
                    title = title,
                    instructions = preferences[INSTRUCTIONS_KEY] ?: "",
                    ingredientIds = preferences[INGREDIENTS_KEY]?.toList() ?: emptyList()
                )
            }
        }
    }

    fun pin(context: Context, recipe: Recipe) {
        _pinnedRecipe.value = recipe
        scope.launch {
            context.dataStore.edit { prefs ->
                prefs[TITLE_KEY] = recipe.title
                prefs[INSTRUCTIONS_KEY] = recipe.instructions
                prefs[INGREDIENTS_KEY] = recipe.ingredientIds.toSet()
            }
            notifyWidget(context)
        }
    }

    fun unpin(context: Context) {
        _pinnedRecipe.value = null
        scope.launch {
            context.dataStore.edit { prefs ->
                prefs.clear()
            }
            notifyWidget(context)
        }
    }

    fun toggle(context: Context, recipe: Recipe) {
        if (_pinnedRecipe.value?.title == recipe.title) unpin(context) else pin(context, recipe)
    }

    fun isPinned(recipe: Recipe): Boolean = _pinnedRecipe.value?.title == recipe.title

    private fun notifyWidget(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(
            android.content.ComponentName(context, com.example.posilkuz.ui.widget.PinnedRecipeWidget::class.java)
        )
        if (ids.isNotEmpty()) {
            val intent = android.content.Intent(context, com.example.posilkuz.ui.widget.PinnedRecipeWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }
}