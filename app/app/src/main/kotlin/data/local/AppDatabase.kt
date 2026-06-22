package com.example.appcantina.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        DailyMealEntity::class,
        FormConfigEntity::class,
        MenuItemEntity::class,
        NewsEntity::class,
        OrderEntity::class,
        OrderLineEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao
    abstract fun orderDao(): OrderDao
    abstract fun formDao(): FormDao
    abstract fun newsDao(): NewsDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_cantina.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE form_config ADD COLUMN orderOpenTime TEXT NOT NULL DEFAULT '19:00'")
                db.execSQL("ALTER TABLE form_config ADD COLUMN orderCloseTime TEXT NOT NULL DEFAULT '08:00'")
                db.execSQL("ALTER TABLE form_config ADD COLUMN autoAcceptOrders INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS news (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        body TEXT NOT NULL,
                        createdAt TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
