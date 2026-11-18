package com.example.tracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [User::class, Category::class, Expense::class], version = 3)
    abstract class AppDatabase:RoomDatabase()   {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE expenses ADD COLUMN userId INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "UPDATE expenses SET userId = " +
                            "COALESCE((SELECT id FROM users ORDER BY id LIMIT 1), 0) " +
                            "WHERE userId = 0"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_expenses_userId ON expenses (userId)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_expenses_categoryId ON expenses (categoryId)"
                )
            }
        }
    }

    fun getInstance(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "expense_manager_db"
            )
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
            INSTANCE = instance
            instance
        }
    }
}