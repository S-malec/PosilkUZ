package com.example.posilkuz.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.posilkuz.R
import com.example.posilkuz.data.repository.PinnedRecipeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Dostawca widżetu ekranu głównego wyświetlającego aktualnie przypiętym przepis.
 *
 * Widżet odczytuje przepis z [PinnedRecipeRepository] i aktualizuje
 * widoki `RemoteViews` układu `widget_pinned_recipe.xml`. Wyświetla tytuł,
 * listę składników i instrukcję przepisu. Jeśli żaden przepis nie jest przypięty,
 * pokazuje komunikat zachęcający do przypięcia. Kliknięcie widżetu otwiera aplikację.
 *
 * Aktualizacja widżetu jest wyzwalana broadcastem wysyłanym przez [PinnedRecipeRepository.pin],
 * [PinnedRecipeRepository.unpin] oraz standardowym cyklem życia widżetu Androida.
 */
class PinnedRecipeWidget : AppWidgetProvider() {

    /**
     * Aktualizuje wszystkie instancje widżetu na ekranie domowym.
     *
     * Asynchronicznie odczytuje przypiętym przepis z [PinnedRecipeRepository]
     * i wypełnia nim widoki RemoteViews dla każdego identyfikatora widżetu.
     * Ustawia PendingIntent otwierający [com.example.posilkuz.MainActivity]
     * po kliknięciu widżetu.
     *
     * @param context kontekst Androida
     * @param appWidgetManager menedżer widżetów używany do aktualizacji widoków
     * @param appWidgetIds tablica identyfikatorów wszystkich instancji widżetu do zaktualizowania
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val recipe = PinnedRecipeRepository.pinnedRecipe.first()

            val launchIntent = android.content.Intent(context, com.example.posilkuz.MainActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            for (id in appWidgetIds) {
                val views = RemoteViews(
                    context.packageName,
                    R.layout.widget_pinned_recipe
                )

                if (recipe != null) {
                    views.setTextViewText(R.id.widget_title, recipe.title)
                    val ingredients = recipe.ingredientIds
                        .joinToString(", ") { it.replace("_", " ") }
                    views.setTextViewText(R.id.widget_ingredients, ingredients)
                    views.setTextViewText(R.id.widget_instructions, recipe.instructions)
                } else {
                    views.setTextViewText(R.id.widget_title, "Brak przypiętego przepisu")
                    views.setTextViewText(R.id.widget_ingredients, "Wejdź w przepisy i przypnij jeden 📌")
                }

                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                appWidgetManager.updateAppWidget(id, views)
            }
        }
    }
}
