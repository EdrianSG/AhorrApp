package com.example.ahorrapp.data.repository

import com.example.ahorrapp.data.dao.ScheduledPaymentDao
import com.example.ahorrapp.data.model.ScheduledPayment
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

class ScheduledPaymentRepository @Inject constructor(
    private val scheduledPaymentDao: ScheduledPaymentDao
) {
    fun getAllByUser(userId: Long): Flow<List<ScheduledPayment>> =
        scheduledPaymentDao.getAllByUser(userId)

    fun getAllActiveByUser(userId: Long): Flow<List<ScheduledPayment>> =
        scheduledPaymentDao.getAllActiveByUser(userId)

    fun getAllInactiveByUser(userId: Long): Flow<List<ScheduledPayment>> =
        scheduledPaymentDao.getAllInactiveByUser(userId)

    suspend fun getPaymentsInRange(userId: Long, startDate: Date, endDate: Date): List<ScheduledPayment> =
        scheduledPaymentDao.getPaymentsInRange(userId, startDate, endDate)

    suspend fun addScheduledPayment(payment: ScheduledPayment): Long =
        scheduledPaymentDao.insert(payment)

    suspend fun updateScheduledPayment(payment: ScheduledPayment) =
        scheduledPaymentDao.update(payment)

    suspend fun deleteScheduledPayment(paymentId: Long) =
        scheduledPaymentDao.delete(paymentId)
} 