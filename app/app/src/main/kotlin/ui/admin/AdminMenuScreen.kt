package com.example.appcantina.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appcantina.data.local.DailyMealEntity
import com.example.appcantina.data.local.FormConfigEntity
import com.example.appcantina.data.local.MenuItemEntity
import com.example.appcantina.data.model.MealType
import com.example.appcantina.data.model.MenuCategory
import com.example.appcantina.ui.components.InfoCard
import com.example.appcantina.ui.components.PriceRow
import com.example.appcantina.ui.components.SectionTitle
import com.example.appcantina.ui.components.categoryLabel
import com.example.appcantina.ui.components.mealLabel
import com.example.appcantina.util.Formatters
import com.example.appcantina.util.OrderWindow

@Composable
fun AdminMenuScreen(
    menuDate: String,
    meals: List<DailyMealEntity>,
    menuItems: List<MenuItemEntity>,
    formConfig: FormConfigEntity?,
    orderWindow: OrderWindow,
    onSaveMeal: (MealType, String, String) -> Unit,
    onSaveItem: (Long, String, MenuCategory, String, Boolean) -> Unit,
    onDeleteItem: (MenuItemEntity) -> Unit,
    onSaveOrderSettings: (Boolean, Boolean, Boolean, String, String, Boolean) -> Unit,
    onSyncApi: () -> Unit
) {
    var editingId by rememberSaveable { mutableStateOf(0L) }
    var itemName by rememberSaveable { mutableStateOf("") }
    var itemPrice by rememberSaveable { mutableStateOf("") }
    var itemCategory by rememberSaveable { mutableStateOf(MenuCategory.DRINK) }
    var available by rememberSaveable { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Admin cardapio",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Almoco de ${Formatters.dateLabel(menuDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = onSyncApi) {
                    Text("API")
                }
            }
        }

        item {
            OrderAvailabilityEditor(
                config = formConfig ?: FormConfigEntity(
                    lunchEnabled = true,
                    dinnerEnabled = false,
                    updatedAt = Formatters.nowIso()
                ),
                orderWindow = orderWindow,
                onSaveOrderSettings = onSaveOrderSettings
            )
        }

        item {
            SectionTitle("Refeicoes")
        }

        MealType.entries.forEach { type ->
            item {
                MealEditor(
                    type = type,
                    meal = meals.firstOrNull { it.type == type.name },
                    onSaveMeal = onSaveMeal
                )
            }
        }

        item {
            SectionTitle("Itens")
        }

        item {
            InfoCard {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = itemPrice,
                    onValueChange = { itemPrice = it },
                    label = { Text("Preco") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MenuCategory.entries.forEach { category ->
                        FilterChip(
                            selected = itemCategory == category,
                            onClick = { itemCategory = category },
                            label = { Text(category.label) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Disponivel")
                    Switch(checked = available, onCheckedChange = { available = it })
                }
                Button(
                    onClick = {
                        onSaveItem(editingId, itemName, itemCategory, itemPrice, available)
                        editingId = 0L
                        itemName = ""
                        itemPrice = ""
                        available = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (editingId == 0L) "Adicionar item" else "Salvar item")
                }
            }
        }

        items(menuItems, key = { it.id }) { item ->
            InfoCard {
                PriceRow(
                    title = item.name,
                    priceCents = item.priceCents,
                    subtitle = "${categoryLabel(item.category)} - ${if (item.available) "disponivel" else "indisponivel"}",
                    trailing = {
                        Row {
                            IconButton(
                                onClick = {
                                    editingId = item.id
                                    itemName = item.name
                                    itemPrice = Formatters.money(item.priceCents)
                                    itemCategory = runCatching { MenuCategory.valueOf(item.category) }.getOrDefault(MenuCategory.DRINK)
                                    available = item.available
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { onDeleteItem(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover")
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun OrderAvailabilityEditor(
    config: FormConfigEntity,
    orderWindow: OrderWindow,
    onSaveOrderSettings: (Boolean, Boolean, Boolean, String, String, Boolean) -> Unit
) {
    var ordersEnabled by rememberSaveable(config.updatedAt) { mutableStateOf(config.lunchEnabled) }
    var localEnabled by rememberSaveable(config.updatedAt) { mutableStateOf(config.localEnabled) }
    var takeawayEnabled by rememberSaveable(config.updatedAt) { mutableStateOf(config.takeawayEnabled) }
    var openTime by rememberSaveable(config.updatedAt) { mutableStateOf(config.orderOpenTime) }
    var closeTime by rememberSaveable(config.updatedAt) { mutableStateOf(config.orderCloseTime) }
    var autoAcceptOrders by rememberSaveable(config.updatedAt) { mutableStateOf(config.autoAcceptOrders) }

    InfoCard {
        SectionTitle("Pedidos de almoco")
        Text(
            text = "Proxima/atual data de pedidos: ${Formatters.dateLabel(orderWindow.orderDateIso)}.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = openTime,
                onValueChange = { openTime = it },
                label = { Text("Liberar") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = closeTime,
                onValueChange = { closeTime = it },
                label = { Text("Bloquear") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (ordersEnabled) "Cardapio liberado" else "Cardapio bloqueado")
            Switch(checked = ordersEnabled, onCheckedChange = { ordersEnabled = it })
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Consumir no local")
            Switch(checked = localEnabled, onCheckedChange = { localEnabled = it })
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Levar")
            Switch(checked = takeawayEnabled, onCheckedChange = { takeawayEnabled = it })
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Aceitar automaticamente")
            Switch(checked = autoAcceptOrders, onCheckedChange = { autoAcceptOrders = it })
        }
        Button(
            onClick = {
                onSaveOrderSettings(
                    ordersEnabled,
                    localEnabled,
                    takeawayEnabled,
                    openTime,
                    closeTime,
                    autoAcceptOrders
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar regras")
        }
    }
}

@Composable
private fun MealEditor(
    type: MealType,
    meal: DailyMealEntity?,
    onSaveMeal: (MealType, String, String) -> Unit
) {
    var description by rememberSaveable(type.name) { mutableStateOf("") }
    var price by rememberSaveable(type.name) { mutableStateOf("") }

    LaunchedEffect(meal?.id, meal?.description, meal?.priceCents) {
        description = meal?.description.orEmpty()
        price = meal?.priceCents?.let { Formatters.money(it) }.orEmpty()
    }

    InfoCard {
        Text(
            text = mealLabel(type.name),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descricao") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Preco") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { onSaveMeal(type, description, price) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar ${type.label.lowercase()}")
        }
    }
}
