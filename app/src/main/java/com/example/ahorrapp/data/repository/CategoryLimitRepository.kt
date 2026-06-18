package com.example.ahorrapp.data.repository

import com.example.ahorrapp.data.dao.CategoryLimitDao
import com.example.ahorrapp.data.model.CategoryLimit
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

class CategoryLimitRepository @Inject constructor(
    private val categoryLimitDao: CategoryLimitDao
) {
    fun getCategoryLimitsByUser(userId: Long): Flow<List<CategoryLimit>> {
        return categoryLimitDao.getCategoryLimitsByUser(userId)
    }
    
    fun getActiveCategoryLimitsByUser(userId: Long): Flow<List<CategoryLimit>> {
        return categoryLimitDao.getActiveCategoryLimitsByUser(userId)
    }
    
    suspend fun getCategoryLimitById(id: Long): CategoryLimit? {
        return categoryLimitDao.getCategoryLimitById(id)
    }
    
    suspend fun getCategoryLimitByCategory(userId: Long, category: String): CategoryLimit? {
        return categoryLimitDao.getCategoryLimitByCategory(userId, category)
    }
    
    suspend fun addCategoryLimit(categoryLimit: CategoryLimit): Result<CategoryLimit> {
        return try {
            val id = categoryLimitDao.insertCategoryLimit(categoryLimit)
            val savedLimit = categoryLimit.copy(id = id)
            Result.success(savedLimit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateCategoryLimit(categoryLimit: CategoryLimit): Result<Unit> {
        return try {
            categoryLimitDao.updateCategoryLimit(categoryLimit)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteCategoryLimit(categoryLimit: CategoryLimit): Result<Unit> {
        return try {
            categoryLimitDao.deleteCategoryLimit(categoryLimit)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun addSpentToLimit(limitId: Long, amount: Double): Result<Unit> {
        return try {
            categoryLimitDao.addSpentToLimit(limitId, amount)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getActiveLimitsByDate(userId: Long, currentDate: Date): List<CategoryLimit> {
        return categoryLimitDao.getActiveLimitsByDate(userId, currentDate)
    }
    
    suspend fun getLimitsNearThreshold(userId: Long): List<CategoryLimit> {
        return categoryLimitDao.getLimitsNearThreshold(userId)
    }
} 