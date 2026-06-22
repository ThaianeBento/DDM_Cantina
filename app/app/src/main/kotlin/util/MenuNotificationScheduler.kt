package com.example.appcantina.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.appcantina.data.local.FormConfigEntity
import java.time.LocalDateTime
import java.time.ZoneId

object MenuNotificationScheduler {
    private const val REQUEST_RELEASED = 3001
    private const val REQUEST_CLOSING_SOON = 3002

    fun schedule(context: Context, config: FormConfigEntity?) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        cancel(context, alarmManager)

        if (config?.lunchEnabled != true) return

        val now = LocalDateTime.now()
        val window = OrderRules.currentWindow(config, now)
        scheduleIfFuture(
            context = context,
            alarmManager = alarmManager,
            action = MenuNotificationReceiver.ACTION_MENU_RELEASED,
            requestCode = REQUEST_RELEASED,
            orderDate = window.orderDateIso,
            triggerAt = window.opensAt,
            now = now
        )
        scheduleIfFuture(
            context = context,
            alarmManager = alarmManager,
            action = MenuNotificationReceiver.ACTION_MENU_CLOSING_SOON,
            requestCode = REQUEST_CLOSING_SOON,
            orderDate = window.orderDateIso,
            triggerAt = window.closesAt.minusMinutes(30),
            now = now
        )
    }

    private fun cancel(context: Context, alarmManager: AlarmManager) {
        alarmManager.cancel(pendingIntent(context, MenuNotificationReceiver.ACTION_MENU_RELEASED, REQUEST_RELEASED, null))
        alarmManager.cancel(pendingIntent(context, MenuNotificationReceiver.ACTION_MENU_CLOSING_SOON, REQUEST_CLOSING_SOON, null))
    }

    private fun scheduleIfFuture(
        context: Context,
        alarmManager: AlarmManager,
        action: String,
        requestCode: Int,
        orderDate: String,
        triggerAt: LocalDateTime,
        now: LocalDateTime
    ) {
        if (!triggerAt.isAfter(now)) return

        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            pendingIntent(context, action, requestCode, orderDate)
        )
    }

    private fun pendingIntent(
        context: Context,
        action: String,
        requestCode: Int,
        orderDate: String?
    ): PendingIntent {
        val intent = Intent(context, MenuNotificationReceiver::class.java)
            .setAction(action)
        if (orderDate != null) {
            intent.putExtra(MenuNotificationReceiver.EXTRA_ORDER_DATE, orderDate)
        }

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
