package com.example.appcantina.data.repository

import androidx.room.withTransaction
import com.example.appcantina.data.local.AppDatabase
import com.example.appcantina.data.local.DailyMealEntity
import com.example.appcantina.data.local.FormConfigEntity
import com.example.appcantina.data.local.MenuItemEntity
import com.example.appcantina.data.local.NewsEntity
import com.example.appcantina.data.local.OrderEntity
import com.example.appcantina.data.local.OrderLineEntity
import com.example.appcantina.data.local.OrderWithLines
import com.example.appcantina.data.model.ConsumptionType
import com.example.appcantina.data.model.MealType
import com.example.appcantina.data.model.MenuCategory
import com.example.appcantina.data.model.OrderStatus
import com.example.appcantina.data.remote.CanteenApi
import com.example.appcantina.data.remote.RemoteOrderRequest
import com.example.appcantina.util.Formatters
import com.example.appcantina.util.OrderRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Locale

class CanteenRepository(
    private val database: AppDatabase,
    private val api: CanteenApi
) {
    private val menuDao = database.menuDao()
    private val orderDao = database.orderDao()
    private val formDao = database.formDao()
    private val newsDao = database.newsDao()
    private val seedMutex = Mutex()

    fun observeTodayMeals(): Flow<List<DailyMealEntity>> {
        return menuDao.observeMealsForDate(Formatters.todayIso())
    }

    fun observeMealsForDate(date: String): Flow<List<DailyMealEntity>> {
        return menuDao.observeMealsForDate(date)
    }

    fun observeItems(): Flow<List<MenuItemEntity>> = menuDao.observeItems()

    fun observeAvailableItems(): Flow<List<MenuItemEntity>> = menuDao.observeAvailableItems()

    fun observeFormConfig(): Flow<FormConfigEntity?> = formDao.observeConfig()

    fun observeNews(): Flow<List<NewsEntity>> = newsDao.observeNews()

    fun observeOrders(email: String, isAdmin: Boolean): Flow<List<OrderWithLines>> {
        return if (isAdmin) orderDao.observeAllOrders() else orderDao.observeOrdersForUser(email)
    }

    suspend fun seedDefaultsIfNeeded(date: String = Formatters.todayIso()) = seedMutex.withLock {
        if (menuDao.mealCountForDate(date) == 0) {
            menuDao.upsertMeal(
                DailyMealEntity(
                    date = date,
                    type = MealType.LUNCH.name,
                    description = "Arroz, feijao, frango grelhado, salada e fruta",
                    priceCents = 1400
                )
            )
            menuDao.upsertMeal(
                DailyMealEntity(
                    date = date,
                    type = MealType.DINNER.name,
                    description = "Macarrao ao molho, legumes assados e suco natural",
                    priceCents = 1300
                )
            )
        }

        if (menuDao.itemCount() == 0) {
            defaultItems().forEach { menuDao.upsertItem(it) }
        }

        if (formDao.getConfig() == null) {
            formDao.upsertConfig(
                FormConfigEntity(
                    lunchEnabled = true,
                    dinnerEnabled = false,
                    updatedAt = Formatters.nowIso()
                )
            )
        }
    }

    suspend fun saveMeal(date: String, type: MealType, description: String, priceCents: Int) {
        val existing = menuDao.getMeal(date, type.name)
        menuDao.upsertMeal(
            DailyMealEntity(
                id = existing?.id ?: 0,
                date = date,
                type = type.name,
                description = description.trim(),
                priceCents = priceCents
            )
        )
    }

    suspend fun saveItem(item: MenuItemEntity) {
        menuDao.upsertItem(item)
    }

    suspend fun deleteItem(item: MenuItemEntity) {
        menuDao.deleteItem(item)
    }

    suspend fun saveFormConfig(config: FormConfigEntity) {
        formDao.upsertConfig(config.copy(updatedAt = Formatters.nowIso()))
    }

    suspend fun saveNews(title: String, body: String) {
        newsDao.upsertNews(
            NewsEntity(
                title = title.trim(),
                body = body.trim(),
                createdAt = Formatters.nowIso()
            )
        )
    }

    suspend fun deleteNews(news: NewsEntity) {
        newsDao.deleteNews(news)
    }

    suspend fun submitOrder(
        userEmail: String,
        mealType: MealType,
        consumptionType: ConsumptionType,
        itemIds: List<Long>
    ): Long {
        val config = formDao.getConfig() ?: FormConfigEntity(updatedAt = Formatters.nowIso())
        val orderWindow = OrderRules.currentWindow(config)

        check(orderWindow.isOpen) {
            "Pedidos disponiveis apenas das ${orderWindow.openTimeLabel} ate ${orderWindow.closeTimeLabel}."
        }
        check(config.lunchEnabled) {
            "O cardapio esta bloqueado para pedidos."
        }
        check(mealType == MealType.LUNCH) {
            "Pedidos disponiveis apenas para almoco."
        }

        val orderDate = orderWindow.orderDateIso
        val lines = buildOrderLines(orderDate, itemIds)
        val totalCents = lines.sumOf { it.quantity * it.unitPriceCents }
        val initialStatus = if (config.autoAcceptOrders) {
            OrderStatus.CONFIRMED
        } else {
            OrderStatus.PENDING
        }

        val orderId = database.withTransaction {
            val orderId = orderDao.insertOrder(
                OrderEntity(
                    userEmail = userEmail,
                    day = orderDate,
                    mealType = MealType.LUNCH.name,
                    consumptionType = consumptionType.name,
                    status = initialStatus.name,
                    totalCents = totalCents,
                    createdAt = Formatters.nowIso()
                )
            )
            orderDao.insertLines(lines.map { it.copy(orderId = orderId) })
            orderId
        }

        runCatching {
            api.sendOrder(
                RemoteOrderRequest(
                    userEmail = userEmail,
                    mealType = MealType.LUNCH.name,
                    consumptionType = consumptionType.name,
                    itemNames = lines.map { it.itemName },
                    totalCents = totalCents
                )
            )
        }

        return orderId
    }

    suspend fun updateOrder(
        userEmail: String,
        orderId: Long,
        consumptionType: ConsumptionType,
        itemIds: List<Long>
    ) {
        val config = formDao.getConfig() ?: FormConfigEntity(updatedAt = Formatters.nowIso())
        val orderWindow = OrderRules.currentWindow(config)
        val order = orderDao.getOrder(orderId) ?: error("Pedido nao encontrado.")

        check(order.userEmail == userEmail) { "Este pedido pertence a outro usuario." }
        check(canUserModifyOrder(order, orderWindow)) {
            "Pedidos so podem ser editados enquanto a janela estiver aberta."
        }

        val lines = buildOrderLines(orderWindow.orderDateIso, itemIds)
        val totalCents = lines.sumOf { it.quantity * it.unitPriceCents }
        val nextStatus = if (config.autoAcceptOrders) OrderStatus.CONFIRMED else OrderStatus.PENDING

        database.withTransaction {
            orderDao.updateOrder(
                orderId = orderId,
                consumptionType = consumptionType.name,
                status = nextStatus.name,
                totalCents = totalCents
            )
            orderDao.deleteLinesForOrder(orderId)
            orderDao.insertLines(lines.map { it.copy(orderId = orderId) })
        }
    }

    suspend fun cancelUserOrder(userEmail: String, orderId: Long) {
        val config = formDao.getConfig() ?: FormConfigEntity(updatedAt = Formatters.nowIso())
        val orderWindow = OrderRules.currentWindow(config)
        val order = orderDao.getOrder(orderId) ?: error("Pedido nao encontrado.")

        check(order.userEmail == userEmail) { "Este pedido pertence a outro usuario." }
        check(canUserModifyOrder(order, orderWindow)) {
            "Pedidos so podem ser cancelados enquanto a janela estiver aberta."
        }

        orderDao.updateStatus(orderId, OrderStatus.CANCELED.name)
    }

    suspend fun refreshMenuFromRemote(): Result<Unit> = runCatching {
        val response = api.todayMenu()
        val menuDate = OrderRules.currentWindow(formDao.getConfig()).orderDateIso
        response.meals.forEach { remote ->
            val type = MealType.valueOf(remote.type.uppercase(Locale.ROOT))
            saveMeal(menuDate, type, remote.description, remote.priceCents)
        }
        response.items.forEach { remote ->
            menuDao.upsertItem(
                MenuItemEntity(
                    name = remote.name,
                    category = remote.category.uppercase(Locale.ROOT),
                    priceCents = remote.priceCents,
                    available = remote.available
                )
            )
        }
        check(menuDao.mealCountForDate(menuDate) > 0) { "A API nao retornou refeicoes para a data do pedido." }
    }

    suspend fun updateOrderStatus(orderId: Long, status: OrderStatus) {
        orderDao.updateStatus(orderId, status.name)
    }

    suspend fun acceptPendingOrdersForDay(day: String) {
        orderDao.updateStatusesForDay(
            day = day,
            mealType = MealType.LUNCH.name,
            fromStatus = OrderStatus.PENDING.name,
            toStatus = OrderStatus.CONFIRMED.name
        )
    }

    suspend fun confirmPendingOrdersFromService(): List<OrderEntity> {
        return emptyList()
    }

    private suspend fun buildOrderLines(
        orderDate: String,
        itemIds: List<Long>
    ): List<OrderLineEntity> {
        val meal = menuDao.getMeal(orderDate, MealType.LUNCH.name)
            ?: error("Nenhum almoco cadastrado para o dia do pedido.")
        val selectedItems = if (itemIds.isEmpty()) {
            emptyList()
        } else {
            val distinctIds = itemIds.distinct()
            val items = menuDao.getItemsByIds(distinctIds)
            check(items.size == distinctIds.size && items.all { it.available }) {
                "Um ou mais itens selecionados nao estao disponiveis."
            }
            items
        }
        val mealLine = OrderLineEntity(
            orderId = 0,
            itemName = "${MealType.LUNCH.label}: ${meal.description}",
            quantity = 1,
            unitPriceCents = meal.priceCents
        )
        val itemLines = selectedItems.map {
            OrderLineEntity(
                orderId = 0,
                itemName = it.name,
                quantity = 1,
                unitPriceCents = it.priceCents
            )
        }

        return listOf(mealLine) + itemLines
    }

    private fun canUserModifyOrder(order: OrderEntity, orderWindow: com.example.appcantina.util.OrderWindow): Boolean {
        val activeStatuses = setOf(OrderStatus.PENDING.name, OrderStatus.CONFIRMED.name)
        return orderWindow.isOpen &&
            order.day == orderWindow.orderDateIso &&
            order.mealType == MealType.LUNCH.name &&
            order.status in activeStatuses
    }

    private fun defaultItems(): List<MenuItemEntity> {
        return listOf(
            MenuItemEntity(name = "Agua sem gas", category = MenuCategory.DRINK.name, priceCents = 300),
            MenuItemEntity(name = "Suco natural", category = MenuCategory.DRINK.name, priceCents = 600),
            MenuItemEntity(name = "Cafe", category = MenuCategory.DRINK.name, priceCents = 250),
            MenuItemEntity(name = "Pastel assado", category = MenuCategory.SAVORY.name, priceCents = 700),
            MenuItemEntity(name = "Empada integral", category = MenuCategory.SAVORY.name, priceCents = 800),
            MenuItemEntity(name = "Pao de queijo", category = MenuCategory.SAVORY.name, priceCents = 500),
            MenuItemEntity(name = "Bolo de banana", category = MenuCategory.SWEET.name, priceCents = 650),
            MenuItemEntity(name = "Cookie", category = MenuCategory.SWEET.name, priceCents = 450),
            MenuItemEntity(name = "Salada de frutas", category = MenuCategory.SWEET.name, priceCents = 750)
        )
    }
}
