package com.example.appcantina.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "form_config")
data class FormConfigEntity(
    @PrimaryKey val id: Int = 1,
    val lunchEnabled: Boolean = true,
    val dinnerEnabled: Boolean = true,
    val localEnabled: Boolean = true,
    val takeawayEnabled: Boolean = true,
    val orderOpenTime: String = "19:00",
    val orderCloseTime: String = "08:00",
    val autoAcceptOrders: Boolean = false,
    val updatedAt: String
)
