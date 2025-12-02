package com.example.ahorrapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "scheduled_payments",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = com.example.ahorrapp.data.model.Wallet::class,
            parentColumns = ["id"],
            childColumns = ["walletId"],
            onDelete = androidx.room.ForeignKey.SET_NULL
        )
    ],
    indices = [androidx.room.Index("walletId")]
)
data class ScheduledPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val description: String,
    val amount: Double,
    val startDate: Date,
    val endDate: Date?,
    val repeatInterval: RepeatInterval,
    val category: String,
    val isActive: Boolean = true,
    val notificationTime: String = "09:00", // Hora por defecto: 9:00 AM
    val isConfirmed: Boolean = false, // Estado de confirmación del pago
    val lastNotificationDate: Date? = null, // Fecha de la última notificación enviada
    val nextNotificationDate: Date? = null, // Fecha de la próxima notificación
    val walletId: Long? = null // Billetera asociada al pago programado
)