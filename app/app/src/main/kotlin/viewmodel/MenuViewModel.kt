package com.example.appcantina.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcantina.data.local.MenuItemEntity
import com.example.appcantina.data.model.MealType
import com.example.appcantina.data.model.MenuCategory
import com.example.appcantina.data.repository.CanteenRepository
import com.example.appcantina.util.Formatters
import kotlinx.coroutines.launch

class MenuViewModel(
    private val repository: CanteenRepository
) : ViewModel() {
    val todayMeals = repository.observeTodayMeals()
    val menuItems = repository.observeItems()

    var message by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            repository.seedDefaultsIfNeeded()
        }
    }

    fun mealsForDate(date: String) = repository.observeMealsForDate(date)

    fun seedDefaultsForDate(date: String) {
        viewModelScope.launch {
            repository.seedDefaultsIfNeeded(date)
        }
    }

    fun saveMeal(date: String, type: MealType, description: String, price: String) {
        val priceCents = Formatters.parsePriceToCents(price)
        if (description.isBlank() || priceCents == null) {
            message = "Preencha a refeicao e um valor valido."
            return
        }

        viewModelScope.launch {
            repository.saveMeal(date, type, description, priceCents)
            message = "Cardapio atualizado."
        }
    }

    fun saveItem(id: Long, name: String, category: MenuCategory, price: String, available: Boolean) {
        val priceCents = Formatters.parsePriceToCents(price)
        if (name.isBlank() || priceCents == null) {
            message = "Preencha o item e um valor valido."
            return
        }

        viewModelScope.launch {
            repository.saveItem(
                MenuItemEntity(
                    id = id,
                    name = name.trim(),
                    category = category.name,
                    priceCents = priceCents,
                    available = available
                )
            )
            message = "Item salvo."
        }
    }

    fun deleteItem(item: MenuItemEntity) {
        viewModelScope.launch {
            repository.deleteItem(item)
            message = "Item removido."
        }
    }

    fun refreshRemoteMenu() {
        viewModelScope.launch {
            message = repository.refreshMenuFromRemote()
                .fold(
                    onSuccess = { "Cardapio sincronizado pela API." },
                    onFailure = { "Nao foi possivel sincronizar a API configurada." }
                )
        }
    }

    fun clearMessage() {
        message = null
    }
}
