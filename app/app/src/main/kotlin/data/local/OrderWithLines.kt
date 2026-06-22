package com.example.appcantina.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class OrderWithLines(
    @Embedded val order: OrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val lines: List<OrderLineEntity>
)
