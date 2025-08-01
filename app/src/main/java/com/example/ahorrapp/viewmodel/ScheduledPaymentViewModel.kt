package com.example.ahorrapp.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.ahorrapp.data.model.RepeatInterval
import com.example.ahorrapp.data.model.ScheduledPayment
import com.example.ahorrapp.data.repository.ScheduledPaymentRepository
import com.example.ahorrapp.receiver.PaymentAlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ScheduledPaymentViewModel @Inject constructor(
    private val repository: ScheduledPaymentRepository,
    private val userId: Long,
    @ApplicationContext private val context: Context
) : ViewModel() {

    init {
        Log.d("ScheduledPaymentViewModel", "Inicializado con userId: $userId")
    }

    // Siempre cargar todos los pagos del usuario
    val scheduledPayments = repository.getAllByUser(userId).asLiveData()

    private val _paymentResult = MutableLiveData<Result<ScheduledPayment>>()
    val paymentResult: LiveData<Result<ScheduledPayment>> = _paymentResult

    fun loadAllPayments() {
        // No es necesario hacer nada, siempre se cargan todos
        Log.d("ScheduledPaymentViewModel", "Cargando todos los pagos para userId: $userId")
    }

    fun addScheduledPayment(
        title: String,
        description: String,
        amount: Double,
        startDate: Date,
        endDate: Date?,
        repeatInterval: RepeatInterval,
        category: String,
        notificationTime: String = "09:00"
    ) {
        viewModelScope.launch {
            try {
                val payment = ScheduledPayment(
                    userId = userId,
                    title = title,
                    description = description,
                    amount = amount,
                    startDate = startDate,
                    endDate = endDate,
                    repeatInterval = repeatInterval,
                    category = category,
                    notificationTime = notificationTime
                )
                val id = repository.addScheduledPayment(payment)
                val savedPayment = payment.copy(id = id)
                PaymentAlarmReceiver.schedulePaymentAlarm(context, savedPayment)
                _paymentResult.value = Result.success(savedPayment)
            } catch (e: Exception) {
                _paymentResult.value = Result.failure(e)
            }
        }
    }

    fun updateScheduledPayment(payment: ScheduledPayment) {
        viewModelScope.launch {
            try {
                repository.updateScheduledPayment(payment)
                PaymentAlarmReceiver.cancelPaymentAlarm(context, payment.id)
                PaymentAlarmReceiver.schedulePaymentAlarm(context, payment)
                _paymentResult.value = Result.success(payment)
            } catch (e: Exception) {
                _paymentResult.value = Result.failure(e)
            }
        }
    }

    fun deletePayment(payment: ScheduledPayment) {
        viewModelScope.launch {
            try {
                repository.deleteScheduledPayment(payment.id)
                PaymentAlarmReceiver.cancelPaymentAlarm(context, payment.id)
                _paymentResult.value = Result.success(payment)
            } catch (e: Exception) {
                _paymentResult.value = Result.failure(e)
            }
        }
    }

    fun deleteScheduledPayment(payment: ScheduledPayment) {
        deletePayment(payment)
    }
} 