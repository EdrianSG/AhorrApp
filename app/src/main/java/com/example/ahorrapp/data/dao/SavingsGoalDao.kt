package com.example.ahorrapp.data.dao

import androidx.room.*
import com.example.ahorrapp.data.model.SavingsGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getSavingsGoalsByUser(userId: Long): Flow<List<SavingsGoal>>
    
    @Query("SELECT * FROM savings_goals WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    fun getActiveSavingsGoalsByUser(userId: Long): Flow<List<SavingsGoal>>
    
    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getSavingsGoalById(id: Long): SavingsGoal?
    
    @Insert
    suspend fun insertSavingsGoal(savingsGoal: SavingsGoal): Long
    
    @Update
    suspend fun updateSavingsGoal(savingsGoal: SavingsGoal)
    
    @Delete
    suspend fun deleteSavingsGoal(savingsGoal: SavingsGoal)
    
    @Query("UPDATE savings_goals SET currentAmount = currentAmount + :amount WHERE id = :goalId")
    suspend fun addAmountToGoal(goalId: Long, amount: Double)
    
    @Query("SELECT SUM(targetAmount) FROM savings_goals WHERE userId = :userId AND isActive = 1")
    suspend fun getTotalTargetAmount(userId: Long): Double?
    
    @Query("SELECT SUM(currentAmount) FROM savings_goals WHERE userId = :userId AND isActive = 1")
    suspend fun getTotalCurrentAmount(userId: Long): Double?
} 