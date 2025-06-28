package com.example.ahorrapp.data.repository

import com.example.ahorrapp.data.dao.SavingsGoalDao
import com.example.ahorrapp.data.model.SavingsGoal
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SavingsGoalRepository @Inject constructor(
    private val savingsGoalDao: SavingsGoalDao
) {
    fun getSavingsGoalsByUser(userId: Long): Flow<List<SavingsGoal>> {
        return savingsGoalDao.getSavingsGoalsByUser(userId)
    }
    
    fun getActiveSavingsGoalsByUser(userId: Long): Flow<List<SavingsGoal>> {
        return savingsGoalDao.getActiveSavingsGoalsByUser(userId)
    }
    
    suspend fun getSavingsGoalById(id: Long): SavingsGoal? {
        return savingsGoalDao.getSavingsGoalById(id)
    }
    
    suspend fun addSavingsGoal(savingsGoal: SavingsGoal): Result<SavingsGoal> {
        return try {
            val id = savingsGoalDao.insertSavingsGoal(savingsGoal)
            val savedGoal = savingsGoal.copy(id = id)
            Result.success(savedGoal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateSavingsGoal(savingsGoal: SavingsGoal): Result<Unit> {
        return try {
            savingsGoalDao.updateSavingsGoal(savingsGoal)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteSavingsGoal(savingsGoal: SavingsGoal): Result<Unit> {
        return try {
            savingsGoalDao.deleteSavingsGoal(savingsGoal)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun addAmountToGoal(goalId: Long, amount: Double): Result<Unit> {
        return try {
            savingsGoalDao.addAmountToGoal(goalId, amount)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTotalTargetAmount(userId: Long): Double {
        return savingsGoalDao.getTotalTargetAmount(userId) ?: 0.0
    }
    
    suspend fun getTotalCurrentAmount(userId: Long): Double {
        return savingsGoalDao.getTotalCurrentAmount(userId) ?: 0.0
    }
} 