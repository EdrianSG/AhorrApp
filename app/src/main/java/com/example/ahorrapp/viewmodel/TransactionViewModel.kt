package com.example.ahorrapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.ahorrapp.data.model.Transaction
import com.example.ahorrapp.data.repository.TransactionRepository
import com.example.ahorrapp.data.repository.CategoryLimitRepository
import com.example.ahorrapp.data.repository.NotificationSettingsRepository
import com.example.ahorrapp.service.EnhancedNotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val categoryLimitRepository: CategoryLimitRepository,
    private val notificationService: EnhancedNotificationService,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val userId: Long
) : ViewModel() {

    val transactions = repository.getTransactionsByUser(userId).asLiveData()

    private val _totalIngresos = MutableLiveData<Double>(0.0)
    val totalIngresos: LiveData<Double> = _totalIngresos

    private val _totalGastos = MutableLiveData<Double>(0.0)
    val totalGastos: LiveData<Double> = _totalGastos

    private val _transactionResult = MutableLiveData<Result<Transaction>?>()
    val transactionResult: LiveData<Result<Transaction>?> = _transactionResult

    init {
        updateTotals()
    }

    fun updateTotals() {
        viewModelScope.launch {
            try {
                val ingresos = repository.getTotalByType(userId, "INGRESO")
                val gastos = repository.getTotalByType(userId, "GASTO")
                _totalIngresos.postValue(ingresos)
                _totalGastos.postValue(gastos)
            } catch (e: Exception) {
                _totalIngresos.postValue(0.0)
                _totalGastos.postValue(0.0)
            }
        }
    }

    fun addTransaction(
        description: String,
        amount: Double,
        type: String,
        category: String,
        date: Date = Date(),
        walletId: Long? = null
    ) {
        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    userId = userId,
                    description = description,
                    amount = amount,
                    type = type,
                    category = category,
                    date = date,
                    walletId = walletId
                )
                val result = repository.addTransaction(transaction)
                if (type == "GASTO") {
                    updateCategoryLimit(category, amount)
                }
                _transactionResult.value = result
                updateTotals()
            } catch (e: Exception) {
                _transactionResult.value = Result.failure(e)
            }
        }
    }
    
    private suspend fun updateCategoryLimit(category: String, amount: Double) {
        try {
            val currentDate = Date()
            val activeLimits = categoryLimitRepository.getActiveLimitsByDate(userId, currentDate)
            val matchingLimit = activeLimits.find { it.category == category }
            
            if (matchingLimit != null) {
                categoryLimitRepository.addSpentToLimit(matchingLimit.id, amount)
                val updatedLimit = categoryLimitRepository.getCategoryLimitById(matchingLimit.id)
                if (updatedLimit != null) {
                    val notificationSettings = notificationSettingsRepository.getOrCreateNotificationSettings(userId)
                    val percentage = ((updatedLimit.currentSpent / updatedLimit.limitAmount) * 100).toInt()
                    if (percentage >= notificationSettings.categoryLimitsThreshold) {
                        notificationService.showCategoryLimitNotification(updatedLimit, notificationSettings)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.updateTransaction(transaction)
                updateTotals()
            } catch (e: Exception) {
                _transactionResult.value = Result.failure(e)
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction)
                updateTotals()
            } catch (e: Exception) {
                _transactionResult.value = Result.failure(e)
            }
        }
    }

    fun clearTransactionResult() {
        _transactionResult.value = null
    }

    fun getCategoryTotals(type: String) = repository.getCategoryTotals(userId, type).asLiveData()

    fun getTransactionsByPeriod(startDate: Date, endDate: Date): LiveData<List<Transaction>> {
        return repository.getTransactionsByPeriod(userId, startDate, endDate).asLiveData()
    }
}
