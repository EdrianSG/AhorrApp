package com.example.ahorrapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ahorrapp.data.model.NotificationSettings
import com.example.ahorrapp.data.repository.NotificationSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val userId: Long
) : ViewModel() {

    private val _settings = MutableStateFlow<NotificationSettings?>(null)
    val settings: StateFlow<NotificationSettings?> = _settings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val settings = notificationSettingsRepository.getOrCreateNotificationSettings(userId)
                _settings.value = settings
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al cargar configuraciones: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSettings(updatedSettings: NotificationSettings) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                notificationSettingsRepository.updateNotificationSettings(updatedSettings)
                _settings.value = updatedSettings
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al actualizar configuraciones: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateScheduledPaymentsEnabled(enabled: Boolean) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(scheduledPaymentsEnabled = enabled))
        }
    }

    fun updateScheduledPaymentsSound(enabled: Boolean) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(scheduledPaymentsSound = enabled))
        }
    }

    fun updateScheduledPaymentsSoundUri(soundUri: String) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(scheduledPaymentsSoundUri = soundUri))
        }
    }

    fun updateScheduledPaymentsVibration(enabled: Boolean) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(scheduledPaymentsVibration = enabled))
        }
    }

    fun updateScheduledPaymentsAdvanceTime(minutes: Int) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(scheduledPaymentsAdvanceTime = minutes))
        }
    }

    fun updateSavingsGoalsEnabled(enabled: Boolean) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(savingsGoalsEnabled = enabled))
        }
    }

    fun updateSavingsGoalsSound(enabled: Boolean) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(savingsGoalsSound = enabled))
        }
    }

    fun updateSavingsGoalsSoundUri(soundUri: String) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(savingsGoalsSoundUri = soundUri))
        }
    }

    fun updateSavingsGoalsVibration(enabled: Boolean) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(savingsGoalsVibration = enabled))
        }
    }

    fun updateCategoryLimitsEnabled(enabled: Boolean) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(categoryLimitsEnabled = enabled))
        }
    }

    fun updateCategoryLimitsSound(enabled: Boolean) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(categoryLimitsSound = enabled))
        }
    }

    fun updateCategoryLimitsSoundUri(soundUri: String) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(categoryLimitsSoundUri = soundUri))
        }
    }

    fun updateCategoryLimitsVibration(enabled: Boolean) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(categoryLimitsVibration = enabled))
        }
    }

    fun updateCategoryLimitsThreshold(threshold: Int) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(categoryLimitsThreshold = threshold))
        }
    }

    fun updateGeneralNotificationsEnabled(enabled: Boolean) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(generalNotificationsEnabled = enabled))
        }
    }

    fun updateGeneralNotificationsSound(enabled: Boolean) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(generalNotificationsSound = enabled))
        }
    }

    fun updateGeneralNotificationsSoundUri(soundUri: String) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(generalNotificationsSoundUri = soundUri))
        }
    }

    fun updateGeneralNotificationsVibration(enabled: Boolean) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(generalNotificationsVibration = enabled))
        }
    }

    fun updateQuietHoursEnabled(enabled: Boolean) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(quietHoursEnabled = enabled))
        }
    }

    fun updateQuietHoursStart(time: String) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(quietHoursStart = time))
        }
    }

    fun updateQuietHoursEnd(time: String) {
        _settings.value?.let { currentSettings ->
            updateSettings(currentSettings.copy(quietHoursEnd = time))
        }
    }

    fun clearError() {
        _error.value = null
    }
} 