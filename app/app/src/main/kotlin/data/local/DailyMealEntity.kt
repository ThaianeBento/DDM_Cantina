package com.example.appcantina.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_meals")
data class DailyMealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val type: String,
    val description: String,
    val priceCents: Int
)
