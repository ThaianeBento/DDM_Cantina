package com.example.appcantina.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Transaction
    @Query("SELECT * FROM orders WHERE userEmail = :email ORDER BY createdAt DESC")
    fun observeOrdersForUser(email: String): Flow<List<OrderWithLines>>

    @Transaction
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeAllOrders(): Flow<List<OrderWithLines>>

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY createdAt ASC")
    suspend fun getOrdersByStatus(status: String): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrder(orderId: Long): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<OrderLineEntity>)

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateStatus(orderId: Long, status: String)

    @Query(
        """
        UPDATE orders
        SET consumptionType = :consumptionType,
            status = :status,
            totalCents = :totalCents
        WHERE id = :orderId
        """
    )
    suspend fun updateOrder(
        orderId: Long,
        consumptionType: String,
        status: String,
        totalCents: Int
    )

    @Query("DELETE FROM order_lines WHERE orderId = :orderId")
    suspend fun deleteLinesForOrder(orderId: Long)

    @Query(
        """
        UPDATE orders
        SET status = :toStatus
        WHERE day = :day
            AND mealType = :mealType
            AND status = :fromStatus
        """
    )
    suspend fun updateStatusesForDay(
        day: String,
        mealType: String,
        fromStatus: String,
        toStatus: String
    )
}
