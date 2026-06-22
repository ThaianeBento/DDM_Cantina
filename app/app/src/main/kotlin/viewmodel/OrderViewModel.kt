package com.example.appcantina.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcantina.data.model.ConsumptionType
import com.example.appcantina.data.model.MealType
import com.example.appcantina.data.model.OrderStatus
import com.example.appcantina.data.repository.CanteenRepository
import kotlinx.coroutines.launch

class OrderViewModel(
    private val repository: CanteenRepository
) : ViewModel() {
    val availableItems = repository.observeAvailableItems()

    var message by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            repository.seedDefaultsIfNeeded()
        }
    }

    fun orders(email: String, isAdmin: Boolean) = repository.observeOrders(email, isAdmin)

    fun submitOrder(
        userEmail: String,
        mealType: MealType,
        consumptionType: ConsumptionType,
        selectedItemIds: List<Long>
    ) {
        viewModelScope.launch {
            val result = runCatching {
                repository.submitOrder(userEmail, mealType, consumptionType, selectedItemIds)
            }
            message = result.fold(
                onSuccess = { orderId ->
                    "Pedido #$orderId enviado. Aguarde a avaliacao do admin."
                },
                onFailure = { it.message ?: "Nao foi possivel enviar o pedido." }
            )
        }
    }

    fun acceptOrder(orderId: Long) {
        updateStatus(orderId, OrderStatus.CONFIRMED, "Pedido aceito.")
    }

    fun rejectOrder(orderId: Long) {
        updateStatus(orderId, OrderStatus.REJECTED, "Pedido recusado.")
    }

    fun cancelOrder(orderId: Long) {
        updateStatus(orderId, OrderStatus.CANCELED, "Pedido cancelado.")
    }

    fun cancelUserOrder(userEmail: String, orderId: Long) {
        viewModelScope.launch {
            val result = runCatching {
                repository.cancelUserOrder(userEmail, orderId)
            }
            message = result.fold(
                onSuccess = { "Pedido cancelado." },
                onFailure = { it.message ?: "Nao foi possivel cancelar o pedido." }
            )
        }
    }

    fun updateUserOrder(
        userEmail: String,
        orderId: Long,
        consumptionType: ConsumptionType,
        selectedItemIds: List<Long>
    ) {
        viewModelScope.launch {
            val result = runCatching {
                repository.updateOrder(userEmail, orderId, consumptionType, selectedItemIds)
            }
            message = result.fold(
                onSuccess = { "Pedido atualizado." },
                onFailure = { it.message ?: "Nao foi possivel atualizar o pedido." }
            )
        }
    }

    fun acceptPendingOrdersForDay(day: String) {
        viewModelScope.launch {
            repository.acceptPendingOrdersForDay(day)
            message = "Pedidos pendentes aceitos."
        }
    }

    private fun updateStatus(orderId: Long, status: OrderStatus, successMessage: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
            message = successMessage
        }
    }

    fun clearMessage() {
        message = null
    }
}
