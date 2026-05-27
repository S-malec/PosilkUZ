package com.example.posilkuz.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.posilkuz.MainActivity // Upewnij się, że to importuje Twoje MainActivity

class NotificationHelper(private val context: Context) {

    private val CHANNEL_ID = "posilkuz_channel_v2"
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Powiadomienia PosiłkUZ",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Główny kanał powiadomień aplikacji"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Tworzymy akcję, która otwiera aplikację po kliknięciu
    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun sendTestNotification() {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Testowe powiadomienie")
            .setContentText("Gratulacje użytkowniku! Zostałeś wybrany jako dzisiejszy zwycięzca darmowego ajfoą 6s, playstation 4 lub samsung galaxy s6")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent()) // Podpinamy akcję kliknięcia
            .setAutoCancel(true) // Powiadomienie nadal zniknie, ale po tym jak otworzy apkę
            .build()

        notificationManager.notify(1, notification)
    }

    fun sendRecipeReminder(recipeName: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Czas na gotowanie!")
            .setContentText("Masz przypięty przepis: $recipeName. Może warto go teraz przygotować?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent()) // Podpinamy akcję kliknięcia
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2, notification)
    }
}