package com.example.ahorrapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.ahorrapp.data.model.CategoryTotal
import com.example.ahorrapp.data.model.Transaction
import com.example.ahorrapp.data.repository.TransactionRepository
import com.example.ahorrapp.data.repository.CategoryLimitRepository
import com.example.ahorrapp.data.repository.NotificationSettingsRepository
import com.example.ahorrapp.service.EnhancedNotificationService
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date

class TransactionViewModel(
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

    private val _transactionResult = MutableLiveData<Result<Transaction>>()
    val transactionResult: LiveData<Result<Transaction>> = _transactionResult

    init {
        viewModelScope.launch {
            updateTotals()
        }
    }

    fun updateTotals() {
        viewModelScope.launch {
            try {
                val ingresos = repository.getTotalByType(userId, "INGRESO")
                val gastos = repository.getTotalByType(userId, "GASTO")
                _totalIngresos.postValue(ingresos)
                _totalGastos.postValue(gastos)
            } catch (e: Exception) {
                // En caso de error, establecer valores por defecto
                _totalIngresos.postValue(0.0)
                _totalGastos.postValue(0.0)
            }
        }
    }

    fun addTransaction(
        description: String,
        amount: Double,
        type: String,
        category: String
    ) {
        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    userId = userId,
                    description = description,
                    amount = amount,
                    type = type,
                    category = category
                )
                val result = repository.addTransaction(transaction)
                
                // Si es un gasto, actualizar el límite de categoría correspondiente
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
            // Buscar si existe un límite activo para esta categoría
            val currentDate = Date()
            val activeLimits = categoryLimitRepository.getActiveLimitsByDate(userId, currentDate)
            
            // Encontrar el límite que coincida con la categoría
            val matchingLimit = activeLimits.find { it.category == category }
            
            if (matchingLimit != null) {
                // Actualizar el gasto en el límite
                categoryLimitRepository.addSpentToLimit(matchingLimit.id, amount)
                
                // Obtener el límite actualizado para verificar si debe mostrar notificación
                val updatedLimit = categoryLimitRepository.getCategoryLimitById(matchingLimit.id)
                if (updatedLimit != null) {
                    // Obtener configuraciones de notificación
                    val notificationSettings = notificationSettingsRepository.getOrCreateNotificationSettings(userId)
                    
                    // Verificar si debe mostrar notificación (cuando se alcance el umbral)
                    val percentage = ((updatedLimit.currentSpent / updatedLimit.limitAmount) * 100).toInt()
                    if (percentage >= notificationSettings.categoryLimitsThreshold) {
                        notificationService.showCategoryLimitNotification(updatedLimit, notificationSettings)
                    }
                }
            }
        } catch (e: Exception) {
            // Log del error pero no fallar la transacción
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

    fun getCategoryTotals(type: String) = repository.getCategoryTotals(userId, type).asLiveData()

    fun getTransactionsByPeriod(startDate: Date, endDate: Date) =
        repository.getTransactionsByPeriod(userId, startDate, endDate).asLiveData()
}

class TransactionViewModelFactory(
    private val repository: TransactionRepository,
    private val categoryLimitRepository: CategoryLimitRepository,
    private val notificationService: EnhancedNotificationService,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val userId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository, categoryLimitRepository, notificationService, notificationSettingsRepository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 