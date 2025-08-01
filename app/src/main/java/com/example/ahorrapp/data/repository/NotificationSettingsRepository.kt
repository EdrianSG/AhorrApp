package com.example.ahorrapp.data.repository

import com.example.ahorrapp.data.dao.NotificationSettingsDao
import com.example.ahorrapp.data.model.NotificationSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSettingsRepository @Inject constructor(
    private val notificationSettingsDao: NotificationSettingsDao
) {
    
    suspend fun getNotificationSettingsByUser(userId: Long): NotificationSettings? {
        return notificationSettingsDao.getNotificationSettingsByUser(userId)
    }
    
    fun getNotificationSettingsByUserFlow(userId: Long): Flow<NotificationSettings?> {
        return notificationSettingsDao.getNotificationSettingsByUserFlow(userId)
    }
    
    suspend fun insertNotificationSettings(settings: NotificationSettings) {
        notificationSettingsDao.insertNotificationSettings(settings)
    }
    
    suspend fun updateNotificationSettings(settings: NotificationSettings) {
        notificationSettingsDao.updateNotificationSettings(settings)
    }
    
    suspend fun deleteNotificationSettingsByUser(userId: Long) {
        notificationSettingsDao.deleteNotificationSettingsByUser(userId)
    }
    
    suspend fun getOrCreateNotificationSettings(userId: Long): NotificationSettings {
        val existingSettings = getNotificationSettingsByUser(userId)
        return if (existingSettings != null) {
            existingSettings
        } else {
            val defaultSettings = NotificationSettings(userId = userId)
            insertNotificationSettings(defaultSettings)
            defaultSettings
        }
    }
} 