package com.example.ahorrapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.ahorrapp.data.model.CategoryLimit
import com.example.ahorrapp.data.repository.CategoryLimitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CategoryLimitViewModel @Inject constructor(
    private val repository: CategoryLimitRepository,
    private val userId: Long
) : ViewModel() {

    val categoryLimits = repository.getCategoryLimitsByUser(userId).asLiveData()
    val activeCategoryLimits = repository.getActiveCategoryLimitsByUser(userId).asLiveData()

    private val _limitResult = MutableLiveData<Result<CategoryLimit>>()
    val limitResult: LiveData<Result<CategoryLimit>> = _limitResult

    private val _nearLimitLimits = MutableLiveData<List<CategoryLimit>>()
    val nearLimitLimits: LiveData<List<CategoryLimit>> = _nearLimitLimits

    fun addCategoryLimit(category: String, limitAmount: Double, startDate: Date, endDate: Date) {
        viewModelScope.launch {
            try {
                val limit = CategoryLimit(
                    userId = userId,
                    category = category,
                    limitAmount = limitAmount,
                    startDate = startDate,
                    endDate = endDate
                )
                val result = repository.addCategoryLimit(limit)
                _limitResult.value = result
            } catch (e: Exception) {
                _limitResult.value = Result.failure(e)
            }
        }
    }

    fun updateCategoryLimit(limit: CategoryLimit) {
        viewModelScope.launch {
            try {
                val result = repository.updateCategoryLimit(limit)
                _limitResult.value = result.map { limit }
            } catch (e: Exception) {
                _limitResult.value = Result.failure(e)
            }
        }
    }

    fun deleteCategoryLimit(limit: CategoryLimit) {
        viewModelScope.launch {
            try {
                val result = repository.deleteCategoryLimit(limit)
                _limitResult.value = result.map { limit }
            } catch (e: Exception) {
                _limitResult.value = Result.failure(e)
            }
        }
    }

    fun addSpentToLimit(limitId: Long, amount: Double) {
        viewModelScope.launch {
            try {
                val result = repository.addSpentToLimit(limitId, amount)
                _limitResult.value = result.map { 
                    CategoryLimit(
                        userId = userId,
                        category = "",
                        limitAmount = 0.0,
                        startDate = Date(),
                        endDate = Date()
                    )
                }
            } catch (e: Exception) {
                _limitResult.value = Result.failure(e)
            }
        }
    }

    fun loadNearLimitLimits() {
        viewModelScope.launch {
            try {
                val limits = repository.getLimitsNearThreshold(userId)
                _nearLimitLimits.value = limits
            } catch (e: Exception) {
                _nearLimitLimits.value = emptyList()
            }
        }
    }

    fun getActiveLimitsByDate(currentDate: Date): LiveData<List<CategoryLimit>> {
        val liveData = MutableLiveData<List<CategoryLimit>>()
        viewModelScope.launch {
            try {
                val limits = repository.getActiveLimitsByDate(userId, currentDate)
                liveData.value = limits
            } catch (e: Exception) {
                liveData.value = emptyList()
            }
        }
        return liveData
    }

    fun checkForNearLimitNotifications() {
        viewModelScope.launch {
            try {
                val nearLimits = repository.getLimitsNearThreshold(userId)
                if (nearLimits.isNotEmpty()) {
                    // Aquí se podrían enviar notificaciones
                    // Por ahora solo actualizamos el LiveData
                    _nearLimitLimits.value = nearLimits
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun loadAllLimits() {
        // Los límites ya se cargan automáticamente a través del Flow
    }

    fun loadActiveLimits() {
        // Los límites activos ya se cargan automáticamente a través del Flow
    }
} 