package com.example.ahorrapp.data.dao

import androidx.room.*
import com.example.ahorrapp.data.model.CategoryTotal
import com.example.ahorrapp.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TransactionDao {
    @Insert
    suspend fun insertTransaction(transaction: Transaction): Long

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getTransactionsByUser(userId: Long): Flow<List<Transaction>>

    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE userId = :userId AND type = :type
    """)
    suspend fun getTotalByType(userId: Long, type: String): Double?

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId AND date >= :startDate AND date <= :endDate 
        ORDER BY date DESC
    """)
    fun getTransactionsByPeriod(userId: Long, startDate: Date, endDate: Date): Flow<List<Transaction>>

    @Query("""
        SELECT category, SUM(amount) as total 
        FROM transactions 
        WHERE userId = :userId AND type = :type 
        GROUP BY category
    """)
    fun getCategoryTotals(userId: Long, type: String): Flow<List<CategoryTotal>>

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteAllTransactionsByUser(userId: Long)

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId AND date >= :startTimestamp AND date <= :endTimestamp 
        ORDER BY date DESC
    """)
    suspend fun getTransactionsByDateRange(userId: Long, startTimestamp: Long, endTimestamp: Long): List<Transaction>
} 