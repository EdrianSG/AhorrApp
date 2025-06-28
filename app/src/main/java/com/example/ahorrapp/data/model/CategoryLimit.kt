package com.example.ahorrapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "category_limits",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CategoryLimit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val category: String,
    val limitAmount: Double,
    val currentSpent: Double = 0.0,
    val startDate: Date,
    val endDate: Date,
    val createdAt: Date = Date(),
    val isActive: Boolean = true
) {
    val progress: Double
        get() = if (limitAmount > 0) (currentSpent / limitAmount) * 100 else 0.0
    
    val remainingAmount: Double
        get() = limitAmount - currentSpent
    
    val isNearLimit: Boolean
        get() = progress >= 90.0
    
    val isOverLimit: Boolean
        get() = currentSpent > limitAmount
} 