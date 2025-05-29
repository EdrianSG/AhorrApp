package com.example.ahorrapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ahorrapp.data.dao.TransactionDao
import com.example.ahorrapp.data.dao.UserDao
import com.example.ahorrapp.data.model.Transaction
import com.example.ahorrapp.data.model.User
import com.example.ahorrapp.data.util.DateConverter

@Database(
    entities = [User::class, Transaction::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ahorrapp_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 