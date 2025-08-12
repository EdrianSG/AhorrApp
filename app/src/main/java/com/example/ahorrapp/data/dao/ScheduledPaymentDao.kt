package com.example.ahorrapp.data.dao

import androidx.room.*
import com.example.ahorrapp.data.model.ScheduledPayment
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ScheduledPaymentDao {
    @Query("SELECT * FROM scheduled_payments WHERE userId = :userId")
    fun getAllByUser(userId: Long): Flow<List<ScheduledPayment>>

    @Query("SELECT * FROM scheduled_payments WHERE userId = :userId AND isActive = 1")
    fun getAllActiveByUser(userId: Long): Flow<List<ScheduledPayment>>

    @Query("SELECT * FROM scheduled_payments WHERE userId = :userId AND isActive = 0")
    fun getAllInactiveByUser(userId: Long): Flow<List<ScheduledPayment>>

    @Query("SELECT * FROM scheduled_payments WHERE id = :paymentId")
    suspend fun getPaymentById(paymentId: Long): ScheduledPayment?

    @Query("SELECT * FROM scheduled_payments WHERE userId = :userId AND startDate >= :startDate AND (endDate IS NULL OR endDate <= :endDate) AND isActive = 1")
    suspend fun getPaymentsInRange(userId: Long, startDate: Date, endDate: Date): List<ScheduledPayment>

    @Query("SELECT * FROM scheduled_payments WHERE userId = :userId AND isConfirmed = 0 AND nextNotificationDate <= :currentDate")
    suspend fun getPendingPayments(userId: Long, currentDate: Date): List<ScheduledPayment>

    @Insert
    suspend fun insert(payment: ScheduledPayment): Long

    @Update
    suspend fun update(payment: ScheduledPayment)

    @Query("DELETE FROM scheduled_payments WHERE id = :paymentId")
    suspend fun delete(paymentId: Long)

    @Query("UPDATE scheduled_payments SET isConfirmed = 1 WHERE id = :paymentId")
    suspend fun confirmPayment(paymentId: Long)

    @Query("UPDATE scheduled_payments SET lastNotificationDate = :notificationDate, nextNotificationDate = :nextDate WHERE id = :paymentId")
    suspend fun updateNotificationDates(paymentId: Long, notificationDate: Date, nextDate: Date)

    @Query("UPDATE scheduled_payments SET isConfirmed = 0 WHERE id = :paymentId")
    suspend fun resetConfirmation(paymentId: Long)
} 