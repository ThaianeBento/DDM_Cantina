package com.example.appcantina.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {
    @Query("SELECT * FROM daily_meals WHERE date = :date ORDER BY type")
    fun observeMealsForDate(date: String): Flow<List<DailyMealEntity>>

    @Query("SELECT * FROM daily_meals WHERE date = :date AND type = :type LIMIT 1")
    suspend fun getMeal(date: String, type: String): DailyMealEntity?

    @Query("SELECT * FROM menu_items ORDER BY category, name")
    fun observeItems(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE available = 1 ORDER BY category, name")
    fun observeAvailableItems(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE id IN (:ids)")
    suspend fun getItemsByIds(ids: List<Long>): List<MenuItemEntity>

    @Query("SELECT COUNT(*) FROM menu_items")
    suspend fun itemCount(): Int

    @Query("SELECT COUNT(*) FROM daily_meals WHERE date = :date")
    suspend fun mealCountForDate(date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeal(meal: DailyMealEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: MenuItemEntity)

    @Delete
    suspend fun deleteItem(item: MenuItemEntity)
}
