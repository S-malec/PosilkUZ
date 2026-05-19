package com.example.posilkuz.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.posilkuz.R  // ← poprawiony import!
import com.example.posilkuz.data.repository.PinnedRecipeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PinnedRecipeWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Czytamy przepis asynchronicznie z DataStore
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
                    views.setTextViewText(R.id.widget_ingredients, "$ingredients")
                    views.setTextViewText(R.id.widget_instructions, "${recipe.instructions}")
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