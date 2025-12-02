package com.example.ahorrapp

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.ahorrapp.utils.ThemeUtils
import dagger.hilt.android.HiltAndroidApp
 
@HiltAndroidApp
class AhorrApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        updateDynamicColors()
    }
    
    private fun updateDynamicColors() {
        val colorTheme = ThemeUtils.getSavedColorTheme(this)
        updateColorsInXml(colorTheme)
    }
    
    private fun updateColorsInXml(colorTheme: String) {
        // Actualizar los colores dinámicos en colors.xml
        // Como no podemos modificar XML directamente, usamos un enfoque diferente
        // Los colores se actualizarán cuando se recree la actividad
    }
} 