package com.example.ahorrapp.data.dao

import androidx.room.*
import com.example.ahorrapp.data.model.NotificationSettings

@Dao
interface NotificationSettingsDao {
    
    @Query("SELECT * FROM notification_settings WHERE userId = :userId")
    suspend fun getNotificationSettingsByUser(userId: Long): NotificationSettings?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationSettings(settings: NotificationSettings)
    
    @Update
    suspend fun updateNotificationSettings(settings: NotificationSettings)
    
    @Query("DELETE FROM notification_settings WHERE userId = :userId")
    suspend fun deleteNotificationSettingsByUser(userId: Long)
    
    @Query("SELECT * FROM notification_settings WHERE userId = :userId")
    fun getNotificationSettingsByUserFlow(userId: Long): kotlinx.coroutines.flow.Flow<NotificationSettings?>
} 