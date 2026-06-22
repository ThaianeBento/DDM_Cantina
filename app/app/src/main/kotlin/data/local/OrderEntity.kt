package com.example.appcantina.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val day: String,
    val mealType: String,
    val consumptionType: String,
    val status: String,
    val totalCents: Int,
    val createdAt: String
)
