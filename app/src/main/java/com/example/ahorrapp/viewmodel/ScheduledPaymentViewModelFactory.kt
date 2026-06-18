package com.example.ahorrapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ahorrapp.data.repository.ScheduledPaymentRepository
import javax.inject.Inject

class ScheduledPaymentViewModelFactory @Inject constructor(
    private val repository: ScheduledPaymentRepository,
    private val userId: Long,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduledPaymentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduledPaymentViewModel(repository, userId, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 