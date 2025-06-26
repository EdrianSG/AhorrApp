package com.example.ahorrapp.viewmodel

import android.content.Context
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

    private val _filterType = MutableStateFlow(FilterType.ALL)
    
    val scheduledPayments = _filterType.flatMapLatest { filterType ->
        when (filterType) {
            FilterType.ALL -> repository.getAllByUser(userId)
            FilterType.ACTIVE -> repository.getAllActiveByUser(userId)
            FilterType.INACTIVE -> repository.getAllInactiveByUser(userId)
        }
    }.asLiveData()

    private val _paymentResult = MutableLiveData<Result<ScheduledPayment>>()
    val paymentResult: LiveData<Result<ScheduledPayment>> = _paymentResult

    fun loadAllPayments() {
        _filterType.value = FilterType.ALL
    }

    fun loadActivePayments() {
        _filterType.value = FilterType.ACTIVE
    }

    fun loadInactivePayments() {
        _filterType.value = FilterType.INACTIVE
    }

    fun addScheduledPayment(
        title: String,
        description: String,
        amount: Double,
        startDate: Date,
        endDate: Date?,
        repeatInterval: RepeatInterval,
        category: String
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
                    category = category
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

    private enum class FilterType {
        ALL, ACTIVE, INACTIVE
    }
} 