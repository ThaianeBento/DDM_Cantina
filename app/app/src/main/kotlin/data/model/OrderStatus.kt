package com.example.appcantina.data.model

enum class OrderStatus(val label: String) {
    PENDING("Pendente"),
    CONFIRMED("Aceito"),
    REJECTED("Recusado"),
    READY("Pronto"),
    DELIVERED("Entregue"),
    CANCELED("Cancelado")
}
