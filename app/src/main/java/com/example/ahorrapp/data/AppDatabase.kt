package com.example.ahorrapp.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ahorrapp.data.converter.DateConverter
import com.example.ahorrapp.data.dao.CategoryLimitDao
import com.example.ahorrapp.data.dao.SavingsGoalDao
import com.example.ahorrapp.data.dao.ScheduledPaymentDao
import com.example.ahorrapp.data.dao.TransactionDao
import com.example.ahorrapp.data.dao.UserDao
import com.example.ahorrapp.data.model.CategoryLimit
import com.example.ahorrapp.data.model.SavingsGoal
import com.example.ahorrapp.data.model.ScheduledPayment
import com.example.ahorrapp.data.model.Transaction
import com.example.ahorrapp.data.model.User

@Database(
    entities = [
        User::class, 
        Transaction::class, 
        ScheduledPayment::class,
        SavingsGoal::class,
        CategoryLimit::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun scheduledPaymentDao(): ScheduledPaymentDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun categoryLimitDao(): CategoryLimitDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "Ejecutando migración 1_2")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        description TEXT NOT NULL,
                        amount REAL NOT NULL,
                        type TEXT NOT NULL,
                        category TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
                    )
                """)
                
                database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_userId ON transactions(userId)")
                Log.d("AppDatabase", "Migración 1_2 completada")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "Ejecutando migración 2_3")
                // Crear tabla de pagos programados
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS scheduled_payments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        amount REAL NOT NULL,
                        startDate INTEGER NOT NULL,
                        endDate INTEGER,
                        repeatInterval TEXT NOT NULL,
                        category TEXT NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
                    )
                """)
                
                // Crear tabla de metas de ahorro
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS savings_goals (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        targetAmount REAL NOT NULL,
                        currentAmount REAL NOT NULL DEFAULT 0.0,
                        targetDate INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
                    )
                """)
                
                // Crear tabla de límites de categoría
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS category_limits (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        category TEXT NOT NULL,
                        limitAmount REAL NOT NULL,
                        currentSpent REAL NOT NULL DEFAULT 0.0,
                        startDate INTEGER NOT NULL,
                        endDate INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
                    )
                """)
                
                // Crear índices
                database.execSQL("CREATE INDEX IF NOT EXISTS index_scheduled_payments_userId ON scheduled_payments(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_savings_goals_userId ON savings_goals(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_category_limits_userId ON category_limits(userId)")
                Log.d("AppDatabase", "Migración 2_3 completada")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Log.d("AppDatabase", "Creando instancia de base de datos")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ahorrapp_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                Log.d("AppDatabase", "Base de datos creada exitosamente")
                INSTANCE = instance
                instance
            }
        }
    }
} 