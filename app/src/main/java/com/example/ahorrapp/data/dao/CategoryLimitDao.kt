package com.example.ahorrapp.data.dao

import androidx.room.*
import com.example.ahorrapp.data.model.CategoryLimit
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface CategoryLimitDao {
    @Query("SELECT * FROM category_limits WHERE userId = :userId ORDER BY createdAt DESC")
    fun getCategoryLimitsByUser(userId: Long): Flow<List<CategoryLimit>>
    
    @Query("SELECT * FROM category_limits WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    fun getActiveCategoryLimitsByUser(userId: Long): Flow<List<CategoryLimit>>
    
    @Query("SELECT * FROM category_limits WHERE id = :id")
    suspend fun getCategoryLimitById(id: Long): CategoryLimit?
    
    @Query("SELECT * FROM category_limits WHERE userId = :userId AND category = :category AND isActive = 1")
    suspend fun getCategoryLimitByCategory(userId: Long, category: String): CategoryLimit?
    
    @Insert
    suspend fun insertCategoryLimit(categoryLimit: CategoryLimit): Long
    
    @Update
    suspend fun updateCategoryLimit(categoryLimit: CategoryLimit)
    
    @Delete
    suspend fun deleteCategoryLimit(categoryLimit: CategoryLimit)
    
    @Query("UPDATE category_limits SET currentSpent = currentSpent + :amount WHERE id = :limitId")
    suspend fun addSpentToLimit(limitId: Long, amount: Double)
    
    @Query("SELECT * FROM category_limits WHERE userId = :userId AND isActive = 1 AND endDate >= :currentDate")
    suspend fun getActiveLimitsByDate(userId: Long, currentDate: Date): List<CategoryLimit>
    
    @Query("SELECT * FROM category_limits WHERE userId = :userId AND isActive = 1 AND (currentSpent / limitAmount) >= 0.9")
    suspend fun getLimitsNearThreshold(userId: Long): List<CategoryLimit>
} 