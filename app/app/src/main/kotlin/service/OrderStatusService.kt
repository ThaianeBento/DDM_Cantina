package com.example.appcantina.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.appcantina.data.local.AppDatabase
import com.example.appcantina.data.remote.RetrofitClient
import com.example.appcantina.data.repository.CanteenRepository
import com.example.appcantina.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class OrderStatusService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: CanteenRepository

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
        repository = CanteenRepository(
            AppDatabase.getDatabase(applicationContext),
            RetrofitClient.api
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            val confirmedOrders = repository.confirmPendingOrdersFromService()
            confirmedOrders.forEach { order ->
                NotificationHelper.showOrderConfirmed(applicationContext, order.id)
            }
            stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
