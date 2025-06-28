package com.example.ahorrapp.data.repository

import android.util.Log
import com.example.ahorrapp.data.dao.ScheduledPaymentDao
import com.example.ahorrapp.data.model.ScheduledPayment
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

class ScheduledPaymentRepository @Inject constructor(
    private val scheduledPaymentDao: ScheduledPaymentDao
) {
    fun getAllByUser(userId: Long): Flow<List<ScheduledPayment>> {
        Log.d("ScheduledPaymentRepository", "getAllByUser: Consultando pagos para userId: $userId")
        return scheduledPaymentDao.getAllByUser(userId)
    }

    fun getAllActiveByUser(userId: Long): Flow<List<ScheduledPayment>> {
        Log.d("ScheduledPaymentRepository", "getAllActiveByUser: Consultando pagos activos para userId: $userId")
        return scheduledPaymentDao.getAllActiveByUser(userId)
    }

    fun getAllInactiveByUser(userId: Long): Flow<List<ScheduledPayment>> {
        Log.d("ScheduledPaymentRepository", "getAllInactiveByUser: Consultando pagos inactivos para userId: $userId")
        return scheduledPaymentDao.getAllInactiveByUser(userId)
    }

    suspend fun getPaymentsInRange(userId: Long, startDate: Date, endDate: Date): List<ScheduledPayment> {
        Log.d("ScheduledPaymentRepository", "getPaymentsInRange: Consultando pagos en rango para userId: $userId")
        return scheduledPaymentDao.getPaymentsInRange(userId, startDate, endDate)
    }

    suspend fun addScheduledPayment(payment: ScheduledPayment): Long {
        Log.d("ScheduledPaymentRepository", "addScheduledPayment: Agregando pago para userId: ${payment.userId}")
        return scheduledPaymentDao.insert(payment)
    }

    suspend fun updateScheduledPayment(payment: ScheduledPayment) {
        Log.d("ScheduledPaymentRepository", "updateScheduledPayment: Actualizando pago ID: ${payment.id} para userId: ${payment.userId}")
        scheduledPaymentDao.update(payment)
    }

    suspend fun deleteScheduledPayment(paymentId: Long) {
        Log.d("ScheduledPaymentRepository", "deleteScheduledPayment: Eliminando pago ID: $paymentId")
        scheduledPaymentDao.delete(paymentId)
    }
} 