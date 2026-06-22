package com.example.appcantina.ui.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appcantina.data.local.DailyMealEntity
import com.example.appcantina.data.local.FormConfigEntity
import com.example.appcantina.data.local.MenuItemEntity
import com.example.appcantina.data.model.ConsumptionType
import com.example.appcantina.data.model.MealType
import com.example.appcantina.data.model.MenuCategory
import com.example.appcantina.ui.components.InfoCard
import com.example.appcantina.ui.components.PriceRow
import com.example.appcantina.ui.components.SectionTitle
import com.example.appcantina.util.Formatters
import com.example.appcantina.util.OrderWindow

@Composable
fun FormScreen(
    menuDate: String,
    config: FormConfigEntity?,
    ordersEnabled: Boolean,
    orderWindow: OrderWindow,
    meals: List<DailyMealEntity>,
    availableItems: List<MenuItemEntity>,
    editingOrderId: Long?,
    initialConsumptionType: ConsumptionType?,
    initialSelectedItemIds: List<Long>,
    onSubmitOrder: (MealType, ConsumptionType, List<Long>) -> Unit
) {
    val currentConfig = config ?: FormConfigEntity(updatedAt = Formatters.nowIso())
    val mealsByType = meals.associateBy { it.type }
    val lunchMeal = mealsByType[MealType.LUNCH.name]
    val allowedConsumption = ConsumptionType.entries.filter {
        (it == ConsumptionType.LOCAL && currentConfig.localEnabled) ||
            (it == ConsumptionType.TAKEAWAY && currentConfig.takeawayEnabled)
    }

    var mealType by rememberSaveable { mutableStateOf(MealType.LUNCH) }
    var consumptionType by rememberSaveable { mutableStateOf(ConsumptionType.LOCAL) }
    val selectedItems = remember { mutableStateListOf<Long>() }
    val initialSelectedItemsKey = initialSelectedItemIds.joinToString(",")

    LaunchedEffect(currentConfig, meals, orderWindow) {
        mealType = MealType.LUNCH
        if (consumptionType !in allowedConsumption && allowedConsumption.isNotEmpty()) {
            consumptionType = allowedConsumption.first()
        }
    }

    LaunchedEffect(editingOrderId, initialConsumptionType, initialSelectedItemsKey) {
        selectedItems.clear()
        selectedItems.addAll(initialSelectedItemIds)
        if (initialConsumptionType != null && initialConsumptionType in allowedConsumption) {
            consumptionType = initialConsumptionType
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (editingOrderId == null) "Formulario de pedido" else "Editar pedido",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            InfoCard {
                SectionTitle("Refeicao do cardapio")
                Text(
                    text = "Almoco de ${Formatters.dateLabel(menuDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!ordersEnabled) {
                    Text(
                        text = "Cardapio bloqueado pelo admin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (!orderWindow.isOpen) {
                    Text(
                        text = "Pedidos disponiveis das ${orderWindow.openTimeLabel} ate ${orderWindow.closeTimeLabel}. Proxima abertura: ${Formatters.dateTimeLabel(orderWindow.opensAtIso)}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (lunchMeal == null) {
                    Text(
                        text = "Nenhum almoco cadastrado para esta data.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = true,
                            onClick = { mealType = MealType.LUNCH }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = MealType.LUNCH.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = lunchMeal.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = Formatters.money(lunchMeal.priceCents),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item {
            InfoCard {
                SectionTitle("Consumo")
                ConsumptionType.entries.forEach { type ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = consumptionType == type,
                            onClick = { if (type in allowedConsumption) consumptionType = type },
                            enabled = type in allowedConsumption
                        )
                        Text(type.label)
                    }
                }
            }
        }

        item {
            SectionTitle("Itens adicionais")
        }

        MenuCategory.entries.forEach { category ->
            val categoryItems = availableItems.filter { it.category == category.name }
            if (categoryItems.isNotEmpty()) {
                item {
                    Text(
                        text = category.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(categoryItems, key = { it.id }) { item ->
                    InfoCard {
                        PriceRow(
                            title = item.name,
                            priceCents = item.priceCents,
                            trailing = {
                                Checkbox(
                                    checked = item.id in selectedItems,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedItems.add(item.id) else selectedItems.remove(item.id)
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = { onSubmitOrder(mealType, consumptionType, selectedItems.toList()) },
                enabled = ordersEnabled && orderWindow.isOpen && lunchMeal != null && allowedConsumption.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (editingOrderId == null) "Enviar pedido" else "Salvar alteracoes")
            }
        }
    }
}
