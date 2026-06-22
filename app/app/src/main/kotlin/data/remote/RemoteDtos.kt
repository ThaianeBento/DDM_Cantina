package com.example.appcantina.data.remote

data class RemoteMenuResponse(
    val meals: List<RemoteMeal>,
    val items: List<RemoteMenuItem>
)

data class RemoteMeal(
    val type: String,
    val description: String,
    val priceCents: Int
)

data class RemoteMenuItem(
    val name: String,
    val category: String,
    val priceCents: Int,
    val available: Boolean = true
)

data class RemoteOrderRequest(
    val userEmail: String,
    val mealType: String,
    val consumptionType: String,
    val itemNames: List<String>,
    val totalCents: Int
)

data class RemoteOrderResponse(
    val remoteId: String,
    val status: String
)

data class RemoteOrderStatusResponse(
    val status: String
)

data class RemoteFormResponse(
    val userEmail: String,
    val mealType: String,
    val consumptionType: String,
    val items: List<String>,
    val createdAt: String
)
