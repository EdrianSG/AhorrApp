package com.example.ahorrapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "savings_goals",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Wallet::class,
            parentColumns = ["id"],
            childColumns = ["walletId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("walletId")]
)
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: Date,
    val createdAt: Date = Date(),
    val isActive: Boolean = true,
    val walletId: Long? = null // Billetera asociada a la meta de ahorro
) {
    val progress: Double
        get() = if (targetAmount > 0) (currentAmount / targetAmount) * 100 else 0.0
    
    val remainingAmount: Double
        get() = targetAmount - currentAmount
    
    val isCompleted: Boolean
        get() = currentAmount >= targetAmount
} 