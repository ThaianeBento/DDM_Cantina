package com.example.appcantina.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MenuNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.createChannels(context)
        val orderDate = intent.getStringExtra(EXTRA_ORDER_DATE).orEmpty()
        when (intent.action) {
            ACTION_MENU_RELEASED -> NotificationHelper.showMenuReleased(context, orderDate)
            ACTION_MENU_CLOSING_SOON -> NotificationHelper.showMenuClosingSoon(context, orderDate)
        }
    }

    companion object {
        const val ACTION_MENU_RELEASED = "com.example.appcantina.MENU_RELEASED"
        const val ACTION_MENU_CLOSING_SOON = "com.example.appcantina.MENU_CLOSING_SOON"
        const val EXTRA_ORDER_DATE = "order_date"
    }
}
