package com.example.ahorrapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_settings")
data class NotificationSettings(
    @PrimaryKey
    val userId: Long,
    val scheduledPaymentsEnabled: Boolean = true,
    val scheduledPaymentsSound: Boolean = true,
    val scheduledPaymentsSoundUri: String = "default", // URI del sonido o "default"
    val scheduledPaymentsVibration: Boolean = true,
    val scheduledPaymentsAdvanceTime: Int = 30, // minutos antes
    val savingsGoalsEnabled: Boolean = true,
    val savingsGoalsSound: Boolean = true,
    val savingsGoalsSoundUri: String = "default", // URI del sonido o "default"
    val savingsGoalsVibration: Boolean = true,
    val categoryLimitsEnabled: Boolean = true,
    val categoryLimitsSound: Boolean = true,
    val categoryLimitsSoundUri: String = "default", // URI del sonido o "default"
    val categoryLimitsVibration: Boolean = true,
    val categoryLimitsThreshold: Int = 90, // porcentaje
    val generalNotificationsEnabled: Boolean = true,
    val generalNotificationsSound: Boolean = true,
    val generalNotificationsSoundUri: String = "default", // URI del sonido o "default"
    val generalNotificationsVibration: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00", // formato HH:mm
    val quietHoursEnd: String = "08:00" // formato HH:mm
) 