package com.example.ahorrapp.di

import android.content.Context
import android.util.Log
import com.example.ahorrapp.data.AppDatabase
import com.example.ahorrapp.data.dao.ScheduledPaymentDao
import com.example.ahorrapp.data.dao.TransactionDao
import com.example.ahorrapp.data.dao.UserDao
import com.example.ahorrapp.data.dao.SavingsGoalDao
import com.example.ahorrapp.data.dao.CategoryLimitDao
import com.example.ahorrapp.data.repository.ScheduledPaymentRepository
import com.example.ahorrapp.data.repository.TransactionRepository
import com.example.ahorrapp.data.repository.UserRepository
import com.example.ahorrapp.data.repository.SavingsGoalRepository
import com.example.ahorrapp.data.repository.CategoryLimitRepository
import com.example.ahorrapp.service.NotificationService
import com.example.ahorrapp.utils.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    fun provideScheduledPaymentDao(database: AppDatabase): ScheduledPaymentDao {
        return database.scheduledPaymentDao()
    }

    @Provides
    fun provideSavingsGoalDao(database: AppDatabase): SavingsGoalDao {
        return database.savingsGoalDao()
    }

    @Provides
    fun provideCategoryLimitDao(database: AppDatabase): CategoryLimitDao {
        return database.categoryLimitDao()
    }

    @Singleton
    @Provides
    fun provideUserRepository(userDao: UserDao): UserRepository {
        return UserRepository(userDao)
    }

    @Singleton
    @Provides
    fun provideTransactionRepository(transactionDao: TransactionDao): TransactionRepository {
        return TransactionRepository(transactionDao)
    }

    @Singleton
    @Provides
    fun provideScheduledPaymentRepository(scheduledPaymentDao: ScheduledPaymentDao): ScheduledPaymentRepository {
        return ScheduledPaymentRepository(scheduledPaymentDao)
    }

    @Singleton
    @Provides
    fun provideSavingsGoalRepository(savingsGoalDao: SavingsGoalDao): SavingsGoalRepository {
        return SavingsGoalRepository(savingsGoalDao)
    }

    @Singleton
    @Provides
    fun provideCategoryLimitRepository(categoryLimitDao: CategoryLimitDao): CategoryLimitRepository {
        return CategoryLimitRepository(categoryLimitDao)
    }

    @Singleton
    @Provides
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Singleton
    @Provides
    fun provideNotificationService(@ApplicationContext context: Context): NotificationService {
        return NotificationService(context)
    }

    @Provides
    fun provideUserId(sessionManager: SessionManager): Long {
        val userId = sessionManager.getUserId()
        Log.d("AppModule", "Proporcionando userId: $userId")
        return userId
    }
} 