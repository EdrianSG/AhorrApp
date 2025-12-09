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
import com.example.ahorrapp.data.dao.NotificationSettingsDao
import com.example.ahorrapp.data.dao.WalletDao
import com.example.ahorrapp.data.dao.SavingsGoalDao
import com.example.ahorrapp.data.dao.ScheduledPaymentDao
import com.example.ahorrapp.data.dao.TransactionDao
import com.example.ahorrapp.data.dao.UserDao
import com.example.ahorrapp.data.model.CategoryLimit
import com.example.ahorrapp.data.model.NotificationSettings
import com.example.ahorrapp.data.model.SavingsGoal
import com.example.ahorrapp.data.model.ScheduledPayment
import com.example.ahorrapp.data.model.Transaction
import com.example.ahorrapp.data.model.Wallet
import com.example.ahorrapp.data.model.User

@Database(
    entities = [
        User::class, 
        Transaction::class, 
        ScheduledPayment::class,
        SavingsGoal::class,
        CategoryLimit::class,
        NotificationSettings::class,
        Wallet::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun scheduledPaymentDao(): ScheduledPaymentDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun categoryLimitDao(): CategoryLimitDao
    abstract fun notificationSettingsDao(): NotificationSettingsDao
    abstract fun walletDao(): WalletDao

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

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "Ejecutando migración 3_4")
                // Crear tabla de configuraciones de notificaciones
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS notification_settings (
                        userId INTEGER PRIMARY KEY NOT NULL,
                        scheduledPaymentsEnabled INTEGER NOT NULL DEFAULT 1,
                        scheduledPaymentsSound INTEGER NOT NULL DEFAULT 1,
                        scheduledPaymentsSoundUri TEXT NOT NULL DEFAULT 'default',
                        scheduledPaymentsVibration INTEGER NOT NULL DEFAULT 1,
                        scheduledPaymentsAdvanceTime INTEGER NOT NULL DEFAULT 30,
                        savingsGoalsEnabled INTEGER NOT NULL DEFAULT 1,
                        savingsGoalsSound INTEGER NOT NULL DEFAULT 1,
                        savingsGoalsSoundUri TEXT NOT NULL DEFAULT 'default',
                        savingsGoalsVibration INTEGER NOT NULL DEFAULT 1,
                        categoryLimitsEnabled INTEGER NOT NULL DEFAULT 1,
                        categoryLimitsSound INTEGER NOT NULL DEFAULT 1,
                        categoryLimitsSoundUri TEXT NOT NULL DEFAULT 'default',
                        categoryLimitsVibration INTEGER NOT NULL DEFAULT 1,
                        categoryLimitsThreshold INTEGER NOT NULL DEFAULT 90,
                        generalNotificationsEnabled INTEGER NOT NULL DEFAULT 1,
                        generalNotificationsSound INTEGER NOT NULL DEFAULT 1,
                        generalNotificationsSoundUri TEXT NOT NULL DEFAULT 'default',
                        generalNotificationsVibration INTEGER NOT NULL DEFAULT 1,
                        quietHoursEnabled INTEGER NOT NULL DEFAULT 0,
                        quietHoursStart TEXT NOT NULL DEFAULT '22:00',
                        quietHoursEnd TEXT NOT NULL DEFAULT '08:00',
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
                    )
                """)
                
                Log.d("AppDatabase", "Migración 3_4 completada")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "Ejecutando migración 4_5")
                // Agregar columna notificationTime a la tabla scheduled_payments
                database.execSQL("ALTER TABLE scheduled_payments ADD COLUMN notificationTime TEXT NOT NULL DEFAULT '09:00'")
                Log.d("AppDatabase", "Migración 4_5 completada")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "Ejecutando migración 5_6")
                // Agregar columnas para confirmación de pagos
                database.execSQL("ALTER TABLE scheduled_payments ADD COLUMN isConfirmed INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE scheduled_payments ADD COLUMN lastNotificationDate INTEGER")
                database.execSQL("ALTER TABLE scheduled_payments ADD COLUMN nextNotificationDate INTEGER")
                Log.d("AppDatabase", "Migración 5_6 completada")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "Ejecutando migración 7_8")
                // Agregar columna walletId a scheduled_payments
                database.execSQL("ALTER TABLE scheduled_payments ADD COLUMN walletId INTEGER")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_scheduled_payments_walletId ON scheduled_payments(walletId)")
                
                // Agregar columna walletId a savings_goals
                database.execSQL("ALTER TABLE savings_goals ADD COLUMN walletId INTEGER")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_savings_goals_walletId ON savings_goals(walletId)")
                
                Log.d("AppDatabase", "Migración 7_8 completada")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("AppDatabase", "Ejecutando migración 8_9")
                // Agregar columna walletId a category_limits
                database.execSQL("ALTER TABLE category_limits ADD COLUMN walletId INTEGER")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_category_limits_walletId ON category_limits(walletId)")
                
                Log.d("AppDatabase", "Migración 8_9 completada")
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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_7_8, MIGRATION_8_9)
                .fallbackToDestructiveMigration()
                .build()
                Log.d("AppDatabase", "Base de datos creada exitosamente")
                INSTANCE = instance
                instance
            }
        }
    }
} 