package com.example.appcantina.util

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.appcantina.R

object NotificationHelper {
    private const val ORDERS_CHANNEL_ID = "orders_channel"
    private const val SERVICE_CHANNEL_ID = "status_service_channel"
    private const val MENU_CHANNEL_ID = "menu_channel"
    const val SERVICE_NOTIFICATION_ID = 10

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val ordersChannel = NotificationChannel(
            ORDERS_CHANNEL_ID,
            "Pedidos",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val serviceChannel = NotificationChannel(
            SERVICE_CHANNEL_ID,
            "Status dos pedidos",
            NotificationManager.IMPORTANCE_LOW
        )
        val menuChannel = NotificationChannel(
            MENU_CHANNEL_ID,
            "Cardapio",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(ordersChannel)
        notificationManager.createNotificationChannel(serviceChannel)
        notificationManager.createNotificationChannel(menuChannel)
    }

    fun serviceNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Cozinha Bem-Estar")
            .setContentText("Verificando status dos pedidos")
            .setOngoing(true)
            .build()
    }

    fun showOrderConfirmed(context: Context, orderId: Long) {
        if (!canShowNotifications(context)) return

        val notification = NotificationCompat.Builder(context, ORDERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Pedido confirmado")
            .setContentText("Seu pedido #$orderId foi confirmado.")
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(orderId.toInt(), notification)
    }

    fun showMenuReleased(context: Context, orderDate: String) {
        if (!canShowNotifications(context)) return

        val notification = NotificationCompat.Builder(context, MENU_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Cardapio liberado")
            .setContentText("Pedidos de almoco para ${Formatters.dateLabel(orderDate)} ja podem ser feitos.")
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(30, notification)
    }

    fun showMenuClosingSoon(context: Context, orderDate: String) {
        if (!canShowNotifications(context)) return

        val notification = NotificationCompat.Builder(context, MENU_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Pedidos encerram em 30 minutos")
            .setContentText("Finalize seu pedido de almoco para ${Formatters.dateLabel(orderDate)}.")
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(31, notification)
    }

    private fun canShowNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }
        return true
    }
}
