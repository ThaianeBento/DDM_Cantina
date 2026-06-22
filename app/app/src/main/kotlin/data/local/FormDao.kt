package com.example.appcantina.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FormDao {
    @Query("SELECT * FROM form_config WHERE id = 1")
    fun observeConfig(): Flow<FormConfigEntity?>

    @Query("SELECT * FROM form_config WHERE id = 1")
    suspend fun getConfig(): FormConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConfig(config: FormConfigEntity)
}
