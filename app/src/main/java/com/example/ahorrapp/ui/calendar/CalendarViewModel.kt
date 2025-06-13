package com.example.ahorrapp.ui.calendar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ahorrapp.data.model.Transaction
import com.example.ahorrapp.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    fun loadTransactionsForDate(userId: Long, timestamp: Long) {
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = timestamp
                
                // Establecer la hora al inicio del día
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis

                // Establecer la hora al final del día
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val endOfDay = calendar.timeInMillis

                val dailyTransactions = transactionRepository.getTransactionsByDateRange(userId, startOfDay, endOfDay)
                _transactions.value = dailyTransactions
            } catch (e: Exception) {
                // En caso de error, establecer una lista vacía
                _transactions.value = emptyList()
            }
        }
    }
} 