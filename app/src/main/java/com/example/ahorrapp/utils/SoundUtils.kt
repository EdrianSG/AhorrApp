package com.example.ahorrapp.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat

object SoundUtils {
    
    // Sonidos del sistema disponibles
    val systemSounds = mapOf(
        "default" to "Sonido por defecto",
        "notification" to "Notificación",
        "alarm" to "Alarma",
        "ringtone" to "Tono de llamada",
        "notification_high" to "Notificación alta",
        "notification_low" to "Notificación baja",
        "notification_medium" to "Notificación media"
    )

    /**
     * Obtiene el URI del sonido del sistema
     */
    fun getSystemSoundUri(soundType: String): Uri {
        return when (soundType) {
            "notification" -> Settings.System.DEFAULT_NOTIFICATION_URI
            "alarm" -> Settings.System.DEFAULT_ALARM_ALERT_URI
            "ringtone" -> Settings.System.DEFAULT_RINGTONE_URI
            "notification_high" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            "notification_low" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            "notification_medium" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            else -> Settings.System.DEFAULT_NOTIFICATION_URI
        }
    }

    /**
     * Configura el sonido para una notificación
     */
    fun configureNotificationSound(builder: NotificationCompat.Builder, soundUri: String) {
        val uri = if (soundUri == "default") {
            Settings.System.DEFAULT_NOTIFICATION_URI
        } else if (soundUri.startsWith("system:")) {
            getSystemSoundUri(soundUri.substring(7))
        } else {
            Uri.parse(soundUri)
        }
        
        builder.setSound(uri)
    }

    /**
     * Obtiene la lista de sonidos del sistema para mostrar en un spinner
     */
    fun getSystemSoundsList(): List<String> {
        return systemSounds.values.toList()
    }

    /**
     * Obtiene la clave del sonido del sistema basado en el nombre
     */
    fun getSystemSoundKey(displayName: String): String {
        return systemSounds.entries.find { it.value == displayName }?.key ?: "default"
    }

    /**
     * Obtiene el nombre de visualización del sonido del sistema
     */
    fun getSystemSoundDisplayName(soundKey: String): String {
        return systemSounds[soundKey] ?: "Sonido por defecto"
    }

    /**
     * Valida si un URI de sonido es válido
     */
    fun isValidSoundUri(context: Context, soundUri: String): Boolean {
        return try {
            if (soundUri == "default" || soundUri.startsWith("system:")) {
                true
            } else {
                val uri = Uri.parse(soundUri)
                context.contentResolver.getType(uri)?.startsWith("audio/") == true
            }
        } catch (e: Exception) {
            false
        }
    }
} 