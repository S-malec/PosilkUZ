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

/** Rozszerzenie dla [Context] zapewniające dostęp do DataStore preferencji dla przypiętego przepisu. */
private val Context.dataStore by preferencesDataStore(name = "pinned_recipe_prefs")

/**
 * Repozytorium singleton zarządzające przypiętym przepisem kulinarnym.
 *
 * Przechowuje jeden przepis wyróżniony przez użytkownika, który jest wyświetlany
 * na ekranie głównym oraz w widżecie aplikacji. Dane są trwale zapisywane
 * w Jetpack DataStore, dzięki czemu przepis jest dostępny po ponownym uruchomieniu aplikacji.
 * Po każdej zmianie rozsyłany jest broadcast aktualizujący widżet na ekranie domowym systemu.
 */
object PinnedRecipeRepository {

    private val _pinnedRecipe = MutableStateFlow<Recipe?>(null)

    /** Reaktywny strumień aktualnie przypiętego przepisu lub `null`, jeśli nic nie jest przypięte. */
    val pinnedRecipe: StateFlow<Recipe?> = _pinnedRecipe.asStateFlow()

    private val TITLE_KEY = stringPreferencesKey("recipe_title")
    private val INSTRUCTIONS_KEY = stringPreferencesKey("recipe_instructions")
    private val INGREDIENTS_KEY = stringSetPreferencesKey("recipe_ingredients")

    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Wczytuje przypiętym przepis z DataStore i inicjalizuje stan repozytorium.
     *
     * Należy wywołać tę funkcję raz przy starcie aplikacji, np. w `MainActivity.onCreate`,
     * aby odtworzyć ostatnio przypiętym przepis.
     *
     * @param context kontekst aplikacji potrzebny do dostępu do DataStore
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

    /**
     * Przypina podany przepis — zapisuje go w DataStore i aktualizuje widżet.
     *
     * @param context kontekst aplikacji potrzebny do dostępu do DataStore i broadcastu widżetu
     * @param recipe przepis do przypięcia
     */
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

    /**
     * Odpina bieżący przepis — usuwa dane z DataStore i aktualizuje widżet.
     *
     * @param context kontekst aplikacji potrzebny do dostępu do DataStore i broadcastu widżetu
     */
    fun unpin(context: Context) {
        _pinnedRecipe.value = null
        scope.launch {
            context.dataStore.edit { prefs ->
                prefs.clear()
            }
            notifyWidget(context)
        }
    }

    /**
     * Przełącza stan przypięcia: odpina przepis, jeśli jest już przypięty, lub przypina go,
     * jeśli nie był wcześniej wybrany.
     *
     * @param context kontekst aplikacji
     * @param recipe przepis do przełączenia
     */
    fun toggle(context: Context, recipe: Recipe) {
        if (_pinnedRecipe.value?.title == recipe.title) unpin(context) else pin(context, recipe)
    }

    /**
     * Sprawdza, czy podany przepis jest aktualnie przypięty.
     *
     * @param recipe przepis do sprawdzenia
     * @return `true`, jeśli przepis jest przypięty; `false` w przeciwnym razie
     */
    fun isPinned(recipe: Recipe): Boolean = _pinnedRecipe.value?.title == recipe.title

    /**
     * Rozsyła broadcast do widżetu aplikacji, aby wymusić jego aktualizację.
     *
     * @param context kontekst aplikacji potrzebny do pobrania identyfikatorów widżetów
     */
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
