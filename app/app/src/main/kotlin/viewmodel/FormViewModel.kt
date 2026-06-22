package com.example.appcantina.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcantina.data.local.FormConfigEntity
import com.example.appcantina.data.repository.CanteenRepository
import com.example.appcantina.util.Formatters
import com.example.appcantina.util.MenuNotificationScheduler
import com.example.appcantina.util.NotificationHelper
import com.example.appcantina.util.OrderRules
import kotlinx.coroutines.launch

class FormViewModel(
    private val repository: CanteenRepository,
    private val appContext: Context
) : ViewModel() {
    val config = repository.observeFormConfig()

    var message by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            repository.seedDefaultsIfNeeded()
        }
    }

    fun saveConfig(
        lunchEnabled: Boolean,
        dinnerEnabled: Boolean,
        localEnabled: Boolean,
        takeawayEnabled: Boolean
    ) {
        if (!lunchEnabled && !dinnerEnabled) {
            message = "Ative pelo menos almoco ou janta."
            return
        }
        if (!localEnabled && !takeawayEnabled) {
            message = "Ative pelo menos uma forma de consumo."
            return
        }

        viewModelScope.launch {
            repository.saveFormConfig(
                FormConfigEntity(
                    lunchEnabled = lunchEnabled,
                    dinnerEnabled = dinnerEnabled,
                    localEnabled = localEnabled,
                    takeawayEnabled = takeawayEnabled,
                    updatedAt = Formatters.nowIso()
                )
            )
            message = "Formulario atualizado."
        }
    }

    fun saveOrderSettings(
        ordersEnabled: Boolean,
        localEnabled: Boolean,
        takeawayEnabled: Boolean,
        orderOpenTime: String,
        orderCloseTime: String,
        autoAcceptOrders: Boolean
    ) {
        if (!localEnabled && !takeawayEnabled) {
            message = "Ative pelo menos uma forma de consumo."
            return
        }
        val normalizedOpenTime = OrderRules.normalizeTime(orderOpenTime)
        val normalizedCloseTime = OrderRules.normalizeTime(orderCloseTime)
        if (normalizedOpenTime == null || normalizedCloseTime == null) {
            message = "Informe horarios validos no formato HH:mm."
            return
        }
        if (normalizedOpenTime == normalizedCloseTime) {
            message = "Horario de abertura e fechamento precisam ser diferentes."
            return
        }

        viewModelScope.launch {
            val config = FormConfigEntity(
                lunchEnabled = ordersEnabled,
                dinnerEnabled = false,
                localEnabled = localEnabled,
                takeawayEnabled = takeawayEnabled,
                orderOpenTime = normalizedOpenTime,
                orderCloseTime = normalizedCloseTime,
                autoAcceptOrders = autoAcceptOrders,
                updatedAt = Formatters.nowIso()
            )
            repository.saveFormConfig(
                config
            )
            MenuNotificationScheduler.schedule(appContext, config)
            val orderWindow = OrderRules.currentWindow(config)
            if (ordersEnabled && orderWindow.isOpen) {
                NotificationHelper.showMenuReleased(appContext, orderWindow.orderDateIso)
            }
            message = "Regras de pedidos atualizadas."
        }
    }

    fun clearMessage() {
        message = null
    }
}
