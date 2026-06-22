package com.example.appcantina.ui.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appcantina.data.local.DailyMealEntity
import com.example.appcantina.data.local.MenuItemEntity
import com.example.appcantina.data.model.MealType
import com.example.appcantina.data.model.MenuCategory
import com.example.appcantina.ui.components.InfoCard
import com.example.appcantina.ui.components.PriceRow
import com.example.appcantina.ui.components.SectionTitle
import com.example.appcantina.ui.components.mealLabel
import com.example.appcantina.util.Formatters
import com.example.appcantina.util.OrderWindow

@Composable
fun MenuScreen(
    menuDate: String,
    meals: List<DailyMealEntity>,
    menuItems: List<MenuItemEntity>,
    ordersEnabled: Boolean,
    orderWindow: OrderWindow,
    onStartOrder: () -> Unit
) {
    val sortedMeals = MealType.entries.mapNotNull { type ->
        meals.firstOrNull { it.type == type.name }
    }
    val hasLunch = meals.any { it.type == MealType.LUNCH.name }
    val canOrder = ordersEnabled && orderWindow.isOpen && hasLunch

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Cardapio do dia",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Almoco de ${Formatters.dateLabel(menuDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = orderAvailabilityText(
                        ordersEnabled = ordersEnabled,
                        orderWindow = orderWindow,
                        hasLunch = hasLunch
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onStartOrder,
                    enabled = canOrder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fazer pedido")
                }
            }
        }

        items(sortedMeals, key = { it.id }) { meal ->
            InfoCard {
                Text(
                    text = mealLabel(meal.type),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(meal.description)
                Text(
                    text = Formatters.money(meal.priceCents),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        MenuCategory.entries.forEach { category ->
            val categoryItems = menuItems.filter { it.category == category.name && it.available }
            if (categoryItems.isNotEmpty()) {
                item {
                    SectionTitle(category.label, modifier = Modifier.padding(top = 8.dp))
                }
                items(categoryItems, key = { it.id }) { item ->
                    InfoCard {
                        PriceRow(
                            title = item.name,
                            priceCents = item.priceCents
                        )
                    }
                }
            }
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

private fun orderAvailabilityText(
    ordersEnabled: Boolean,
    orderWindow: OrderWindow,
    hasLunch: Boolean
): String {
    return when {
        !ordersEnabled -> "Pedidos bloqueados pelo admin."
        !orderWindow.isOpen -> "Pedidos disponiveis das ${orderWindow.openTimeLabel} ate ${orderWindow.closeTimeLabel}. Proxima abertura: ${Formatters.dateTimeLabel(orderWindow.opensAtIso)}."
        !hasLunch -> "Cadastre o almoco para liberar pedidos."
        else -> "Pedidos abertos ate ${Formatters.dateTimeLabel(orderWindow.closesAtIso)}."
    }
}
