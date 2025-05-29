package com.example.ahorrapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val description: String,
    val amount: Double,
    val type: String, // "INGRESO" o "GASTO"
    val category: String,
    val date: Date = Date()
) 