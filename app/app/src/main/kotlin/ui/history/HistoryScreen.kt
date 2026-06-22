package com.example.appcantina.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appcantina.data.local.OrderWithLines
import com.example.appcantina.data.model.MealType
import com.example.appcantina.data.model.OrderStatus
import com.example.appcantina.ui.components.InfoCard
import com.example.appcantina.ui.components.consumptionLabel
import com.example.appcantina.ui.components.mealLabel
import com.example.appcantina.ui.components.statusLabel
import com.example.appcantina.util.Formatters
import com.example.appcantina.util.OrderPrintHelper

@Composable
fun HistoryScreen(
    orders: List<OrderWithLines>,
    isAdmin: Boolean,
    orderDate: String,
    canModifyUserOrders: Boolean,
    onAcceptOrder: (Long) -> Unit,
    onRejectOrder: (Long) -> Unit,
    onCancelOrder: (Long) -> Unit,
    onAcceptPendingOrders: (String) -> Unit,
    onEditUserOrder: (OrderWithLines) -> Unit,
    onCancelUserOrder: (Long) -> Unit
) {
    val context = LocalContext.current
    val displayedOrders = if (isAdmin) {
        orders.filter { it.order.day == orderDate && it.order.mealType == MealType.LUNCH.name }
    } else {
        orders
    }
    val pendingOrders = displayedOrders.filter { it.order.status == OrderStatus.PENDING.name }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (isAdmin) "Pedidos do almoco" else "Historico de pedidos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (isAdmin) {
                Text(
                    text = Formatters.dateLabel(orderDate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isAdmin) {
            item {
                InfoCard {
                    Text(
                        text = "Organizacao diaria",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${displayedOrders.size} pedidos listados, ${pendingOrders.size} pendentes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = {
                            OrderPrintHelper.printDailyOrders(context, orderDate, displayedOrders)
                        },
                        enabled = displayedOrders.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Imprimir relacao")
                    }
                    Button(
                        onClick = { onAcceptPendingOrders(orderDate) },
                        enabled = pendingOrders.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Aceitar todos os pendentes")
                    }
                }
            }
        }

        if (displayedOrders.isEmpty()) {
            item {
                InfoCard {
                    Text("Nenhum pedido registrado.")
                }
            }
        }

        items(displayedOrders, key = { it.order.id }) { orderWithLines ->
            val order = orderWithLines.order
            val activeOrder = order.status == OrderStatus.PENDING.name || order.status == OrderStatus.CONFIRMED.name
            val canUserModifyOrder = !isAdmin &&
                canModifyUserOrders &&
                order.day == orderDate &&
                activeOrder
            InfoCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pedido #${order.id}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = Formatters.dateTimeLabel(order.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AssistChip(
                        onClick = {},
                        label = { Text(statusLabel(order.status)) }
                    )
                }
                if (isAdmin) {
                    Text(
                        text = order.userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text("${mealLabel(order.mealType)} - ${consumptionLabel(order.consumptionType)}")
                orderWithLines.lines.forEach { line ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${line.quantity}x ${line.itemName}", modifier = Modifier.weight(1f))
                        Text(Formatters.money(line.quantity * line.unitPriceCents))
                    }
                }
                Text(
                    text = "Total: ${Formatters.money(order.totalCents)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (isAdmin && order.status == OrderStatus.PENDING.name) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onAcceptOrder(order.id) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Aceitar")
                        }
                        OutlinedButton(
                            onClick = { onRejectOrder(order.id) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Recusar")
                        }
                    }
                }
                if (isAdmin && activeOrder) {
                    OutlinedButton(
                        onClick = { onCancelOrder(order.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar pedido")
                    }
                }
                if (canUserModifyOrder) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onEditUserOrder(orderWithLines) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Editar")
                        }
                        OutlinedButton(
                            onClick = { onCancelUserOrder(order.id) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                    }
                }
            }
        }
    }
}
