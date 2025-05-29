package com.example.ahorrapp.data.repository

import com.example.ahorrapp.data.dao.TransactionDao
import com.example.ahorrapp.data.model.CategoryTotal
import com.example.ahorrapp.data.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Date

class TransactionRepository(private val transactionDao: TransactionDao) {

    fun getTransactionsByUser(userId: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByUser(userId)
    }

    suspend fun getTotalByType(userId: Long, type: String): Double {
        return withContext(Dispatchers.IO) {
            transactionDao.getTotalByType(userId, type) ?: 0.0
        }
    }

    fun getTransactionsByPeriod(userId: Long, startDate: Date, endDate: Date): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByPeriod(userId, startDate, endDate)
    }

    fun getCategoryTotals(userId: Long, type: String): Flow<List<CategoryTotal>> {
        return transactionDao.getCategoryTotals(userId, type)
    }

    suspend fun addTransaction(transaction: Transaction): Result<Transaction> {
        return withContext(Dispatchers.IO) {
            try {
                val id = transactionDao.insertTransaction(transaction)
                Result.success(transaction.copy(id = id))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                transactionDao.updateTransaction(transaction)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteTransaction(transaction: Transaction): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                transactionDao.deleteTransaction(transaction)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteAllTransactionsByUser(userId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                transactionDao.deleteAllTransactionsByUser(userId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
} 