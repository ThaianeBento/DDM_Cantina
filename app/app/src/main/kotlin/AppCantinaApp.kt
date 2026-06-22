package com.example.appcantina

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.appcantina.data.model.AuthState
import com.example.appcantina.data.model.ConsumptionType
import com.example.appcantina.ui.admin.AdminMenuScreen
import com.example.appcantina.ui.components.MessageEffect
import com.example.appcantina.ui.form.FormScreen
import com.example.appcantina.ui.history.HistoryScreen
import com.example.appcantina.ui.home.HomeScreen
import com.example.appcantina.ui.login.LoginScreen
import com.example.appcantina.ui.menu.MenuScreen
import com.example.appcantina.ui.navigation.Routes
import com.example.appcantina.ui.theme.AppCantinaTheme
import com.example.appcantina.util.MenuNotificationScheduler
import com.example.appcantina.util.OrderRules
import com.example.appcantina.viewmodel.AppViewModelFactory
import com.example.appcantina.viewmodel.FormViewModel
import com.example.appcantina.viewmodel.MenuViewModel
import com.example.appcantina.viewmodel.NewsViewModel
import com.example.appcantina.viewmodel.OrderViewModel
import com.example.appcantina.viewmodel.SessionViewModel
import kotlinx.coroutines.delay

@Composable
fun AppCantinaApp() {
    val context = LocalContext.current
    val factory = remember(context) { AppViewModelFactory(context) }
    val sessionViewModel: SessionViewModel = viewModel(factory = factory)

    AppCantinaTheme {
        val authState = sessionViewModel.authState
        if (authState == null) {
            LoginScreen(
                message = sessionViewModel.message,
                onLogin = sessionViewModel::login,
                onCreateAccount = sessionViewModel::createAccount,
                showTestAccess = BuildConfig.DEBUG,
                onTestUserLogin = { sessionViewModel.login("teste@udesc.br", "1234") },
                onTestAdminLogin = { sessionViewModel.login("admin@udesc.br", "1234") }
            )
        } else {
            MainScaffold(
                authState = authState,
                factory = factory,
                onLogout = sessionViewModel::logout
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(
    authState: AuthState,
    factory: AppViewModelFactory,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    var editingOrderId by remember { mutableStateOf<Long?>(null) }
    val menuViewModel: MenuViewModel = viewModel(factory = factory)
    val newsViewModel: NewsViewModel = viewModel(factory = factory)
    val formViewModel: FormViewModel = viewModel(factory = factory)
    val orderViewModel: OrderViewModel = viewModel(factory = factory)
    val formConfig by formViewModel.config.collectAsStateWithLifecycle(null)
    val orderWindow by produceState(
        initialValue = OrderRules.currentWindow(formConfig),
        formConfig?.orderOpenTime,
        formConfig?.orderCloseTime
    ) {
        while (true) {
            value = OrderRules.currentWindow(formConfig)
            delay(60_000)
        }
    }
    val orderDate = orderWindow.orderDateIso

    LaunchedEffect(orderDate) {
        menuViewModel.seedDefaultsForDate(orderDate)
    }
    LaunchedEffect(
        formConfig?.updatedAt,
        formConfig?.lunchEnabled,
        formConfig?.orderOpenTime,
        formConfig?.orderCloseTime
    ) {
        MenuNotificationScheduler.schedule(context, formConfig)
    }

    val meals by remember(orderDate) {
        menuViewModel.mealsForDate(orderDate)
    }.collectAsStateWithLifecycle(emptyList())
    val news by newsViewModel.news.collectAsStateWithLifecycle(emptyList())
    val menuItems by menuViewModel.menuItems.collectAsStateWithLifecycle(emptyList())
    val ordersEnabled = formConfig?.lunchEnabled ?: true
    val availableItems by orderViewModel.availableItems.collectAsStateWithLifecycle(emptyList())
    val orders by remember(authState.email, authState.isAdmin) {
        orderViewModel.orders(authState.email, authState.isAdmin)
    }.collectAsStateWithLifecycle(emptyList())
    val editingOrder = remember(editingOrderId, orders) {
        editingOrderId?.let { id -> orders.firstOrNull { it.order.id == id } }
    }
    val editingConsumptionType = remember(editingOrder) {
        editingOrder?.order?.consumptionType?.let {
            runCatching { ConsumptionType.valueOf(it) }.getOrNull()
        }
    }
    val editingSelectedItemIds = remember(editingOrder, availableItems) {
        val lines = editingOrder?.lines.orEmpty()
        availableItems
            .filter { item -> lines.any { line -> line.itemName == item.name } }
            .map { it.id }
    }

    MessageEffect(menuViewModel.message, snackbarHostState, menuViewModel::clearMessage)
    MessageEffect(newsViewModel.message, snackbarHostState, newsViewModel::clearMessage)
    MessageEffect(formViewModel.message, snackbarHostState, formViewModel::clearMessage)
    MessageEffect(orderViewModel.message, snackbarHostState, orderViewModel::clearMessage)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cozinha Bem-Estar") },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Sair")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavigation(
                navController = navController,
                isAdmin = authState.isAdmin
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    authState = authState,
                    news = news,
                    onSaveNews = newsViewModel::saveNews,
                    onDeleteNews = newsViewModel::deleteNews,
                    onOpenMenu = { navController.navigateSingleTop(Routes.MENU) },
                    onOpenForm = {
                        editingOrderId = null
                        navController.navigateSingleTop(Routes.FORM)
                    },
                    onOpenHistory = { navController.navigateSingleTop(Routes.HISTORY) },
                    onOpenAdmin = { navController.navigateSingleTop(Routes.ADMIN_MENU) }
                )
            }
            composable(Routes.MENU) {
                MenuScreen(
                    menuDate = orderDate,
                    meals = meals,
                    menuItems = menuItems,
                    ordersEnabled = ordersEnabled,
                    orderWindow = orderWindow,
                    onStartOrder = {
                        editingOrderId = null
                        navController.navigateSingleTop(Routes.FORM)
                    }
                )
            }
            composable(Routes.FORM) {
                FormScreen(
                    menuDate = orderDate,
                    config = formConfig,
                    ordersEnabled = ordersEnabled,
                    orderWindow = orderWindow,
                    meals = meals,
                    availableItems = availableItems,
                    editingOrderId = editingOrderId,
                    initialConsumptionType = editingConsumptionType,
                    initialSelectedItemIds = editingSelectedItemIds,
                    onSubmitOrder = { mealType, consumptionType, selectedItemIds ->
                        val orderId = editingOrderId
                        if (orderId == null) {
                            orderViewModel.submitOrder(
                                userEmail = authState.email,
                                mealType = mealType,
                                consumptionType = consumptionType,
                                selectedItemIds = selectedItemIds
                            )
                        } else {
                            orderViewModel.updateUserOrder(
                                userEmail = authState.email,
                                orderId = orderId,
                                consumptionType = consumptionType,
                                selectedItemIds = selectedItemIds
                            )
                            editingOrderId = null
                        }
                    }
                )
            }
            composable(Routes.HISTORY) {
                HistoryScreen(
                    orders = orders,
                    isAdmin = authState.isAdmin,
                    orderDate = orderDate,
                    canModifyUserOrders = orderWindow.isOpen,
                    onAcceptOrder = orderViewModel::acceptOrder,
                    onRejectOrder = orderViewModel::rejectOrder,
                    onCancelOrder = orderViewModel::cancelOrder,
                    onAcceptPendingOrders = orderViewModel::acceptPendingOrdersForDay,
                    onEditUserOrder = { orderWithLines ->
                        editingOrderId = orderWithLines.order.id
                        navController.navigateSingleTop(Routes.FORM)
                    },
                    onCancelUserOrder = { orderId ->
                        orderViewModel.cancelUserOrder(authState.email, orderId)
                    }
                )
            }
            if (authState.isAdmin) {
                composable(Routes.ADMIN_MENU) {
                    AdminMenuScreen(
                        menuDate = orderDate,
                        meals = meals,
                        menuItems = menuItems,
                        formConfig = formConfig,
                        orderWindow = orderWindow,
                        onSaveMeal = { type, description, price ->
                            menuViewModel.saveMeal(orderDate, type, description, price)
                        },
                        onSaveItem = menuViewModel::saveItem,
                        onDeleteItem = menuViewModel::deleteItem,
                        onSaveOrderSettings = formViewModel::saveOrderSettings,
                        onSyncApi = menuViewModel::refreshRemoteMenu
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavigation(
    navController: NavHostController,
    isAdmin: Boolean
) {
    val baseItems = listOf(
        BottomItem(Routes.HOME, "Inicio", Icons.Default.Home),
        BottomItem(Routes.MENU, "Cardapio", Icons.Default.List),
        BottomItem(Routes.HISTORY, "Historico", Icons.Default.History)
    )
    val items = if (isAdmin) {
        baseItems + BottomItem(Routes.ADMIN_MENU, "Admin", Icons.Default.Settings)
    } else {
        baseItems
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route || (currentRoute == Routes.FORM && item.route == Routes.MENU),
                onClick = { navController.navigateSingleTop(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(Routes.HOME) {
            saveState = true
        }
    }
}

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)
