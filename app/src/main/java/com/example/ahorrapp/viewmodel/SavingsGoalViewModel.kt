package com.example.ahorrapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.switchMap
import com.example.ahorrapp.data.model.SavingsGoal
import com.example.ahorrapp.data.model.Transaction
import com.example.ahorrapp.data.repository.SavingsGoalRepository
import com.example.ahorrapp.data.repository.TransactionRepository
import com.example.ahorrapp.data.repository.NotificationSettingsRepository
import com.example.ahorrapp.service.EnhancedNotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class SavingsGoalViewModel @Inject constructor(
    private val repository: SavingsGoalRepository,
    private val transactionRepository: TransactionRepository,
    private val notificationService: EnhancedNotificationService,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val userId: Long
) : ViewModel() {

    private val _refreshTrigger = MutableLiveData(Unit)
    val savingsGoals = _refreshTrigger.switchMap {
        repository.getSavingsGoalsByUser(userId).asLiveData()
    }
    val activeSavingsGoals = repository.getActiveSavingsGoalsByUser(userId).asLiveData()

    private val _goalResult = MutableLiveData<Result<SavingsGoal>>()
    val goalResult: LiveData<Result<SavingsGoal>> = _goalResult

    private val _addMoneyResult = MutableLiveData<Result<Unit>>()
    val addMoneyResult: LiveData<Result<Unit>> = _addMoneyResult

    fun refreshGoals() {
        _refreshTrigger.value = Unit
    }

    fun addSavingsGoal(name: String, targetAmount: Double, targetDate: Date) {
        viewModelScope.launch {
            try {
                val goal = SavingsGoal(
                    userId = userId,
                    name = name,
                    targetAmount = targetAmount,
                    targetDate = targetDate
                )
                val result = repository.addSavingsGoal(goal)
                _goalResult.value = result
                refreshGoals()
            } catch (e: Exception) {
                _goalResult.value = Result.failure(e)
            }
        }
    }

    fun updateSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            try {
                val result = repository.updateSavingsGoal(goal)
                _goalResult.value = result.map { goal }
                refreshGoals()
            } catch (e: Exception) {
                _goalResult.value = Result.failure(e)
            }
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            try {
                val result = repository.deleteSavingsGoal(goal)
                _goalResult.value = result.map { goal }
                refreshGoals()
            } catch (e: Exception) {
                _goalResult.value = Result.failure(e)
            }
        }
    }

    fun addMoneyToGoal(goalId: Long, amount: Double, goalName: String) {
        viewModelScope.launch {
            try {
                // 1. Agregar dinero a la meta
                val result = repository.addAmountToGoal(goalId, amount)
                
                if (result.isSuccess) {
                    // 2. Crear una transacción de tipo GASTO para registrar el movimiento
                    val transaction = Transaction(
                        userId = userId,
                        description = "Ahorro: $goalName",
                        amount = amount,
                        type = "GASTO",
                        category = "Ahorro"
                    )
                    
                    transactionRepository.addTransaction(transaction)
                    
                    // 3. Obtener la meta actualizada para mostrar notificación
                    val updatedGoal = repository.getSavingsGoalById(goalId)
                    if (updatedGoal != null) {
                        // 4. Obtener configuraciones de notificación
                        val notificationSettings = notificationSettingsRepository.getOrCreateNotificationSettings(userId)
                        
                        // 5. Mostrar notificación
                        notificationService.showSavingsGoalNotification(updatedGoal, notificationSettings)
                    }
                    
                    _addMoneyResult.value = Result.success(Unit)
                    refreshGoals()
                } else {
                    _addMoneyResult.value = Result.failure(result.exceptionOrNull() ?: Exception("Error desconocido"))
                }
            } catch (e: Exception) {
                _addMoneyResult.value = Result.failure(e)
            }
        }
    }

    fun getTotalTargetAmount(): LiveData<Double> {
        val liveData = MutableLiveData<Double>()
        viewModelScope.launch {
            try {
                val total = repository.getTotalTargetAmount(userId)
                liveData.value = total
            } catch (e: Exception) {
                liveData.value = 0.0
            }
        }
        return liveData
    }

    fun getTotalCurrentAmount(): LiveData<Double> {
        val liveData = MutableLiveData<Double>()
        viewModelScope.launch {
            try {
                val total = repository.getTotalCurrentAmount(userId)
                liveData.value = total
            } catch (e: Exception) {
                liveData.value = 0.0
            }
        }
        return liveData
    }
} 