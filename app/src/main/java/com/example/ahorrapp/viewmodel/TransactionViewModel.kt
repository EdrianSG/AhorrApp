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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date

class TransactionViewModel(
    private val repository: TransactionRepository,
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
                _transactionResult.value = result
                updateTotals()
            } catch (e: Exception) {
                _transactionResult.value = Result.failure(e)
            }
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
    private val userId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 