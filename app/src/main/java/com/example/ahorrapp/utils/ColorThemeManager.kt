package com.example.ahorrapp.utils

import android.content.Context
import android.content.res.Resources
import com.example.ahorrapp.R

object ColorThemeManager {
    
    /**
     * Actualiza los colores por defecto según el tema seleccionado
     * Nota: Esto no modifica el XML directamente, pero permite obtener los colores correctos
     */
    fun getPrimaryColor(context: Context): Int {
        val colorTheme = ThemeUtils.getSavedColorTheme(context)
        return when (colorTheme) {
            "blue" -> context.getColor(R.color.blue_primary_500)
            "red" -> context.getColor(R.color.red_primary_500)
            "purple" -> context.getColor(R.color.purple_primary_500)
            "orange" -> context.getColor(R.color.orange_primary_500)
            "pink" -> context.getColor(R.color.pink_primary_500)
            else -> context.getColor(R.color.green_primary_500)
        }
    }
    
    fun getPrimaryVariantColor(context: Context): Int {
        val colorTheme = ThemeUtils.getSavedColorTheme(context)
        return when (colorTheme) {
            "blue" -> context.getColor(R.color.blue_primary_700)
            "red" -> context.getColor(R.color.red_primary_700)
            "purple" -> context.getColor(R.color.purple_primary_700)
            "orange" -> context.getColor(R.color.orange_primary_700)
            "pink" -> context.getColor(R.color.pink_primary_700)
            else -> context.getColor(R.color.green_primary_700)
        }
    }
    
    fun getPrimaryLightColor(context: Context): Int {
        val colorTheme = ThemeUtils.getSavedColorTheme(context)
        return when (colorTheme) {
            "blue" -> context.getColor(R.color.blue_primary_200)
            "red" -> context.getColor(R.color.red_primary_200)
            "purple" -> context.getColor(R.color.purple_primary_200)
            "orange" -> context.getColor(R.color.orange_primary_200)
            "pink" -> context.getColor(R.color.pink_primary_200)
            else -> context.getColor(R.color.green_primary_200)
        }
    }
    
    fun getSecondaryColor(context: Context): Int {
        val colorTheme = ThemeUtils.getSavedColorTheme(context)
        return when (colorTheme) {
            "blue" -> context.getColor(R.color.blue_secondary_200)
            "red" -> context.getColor(R.color.red_secondary_200)
            "purple" -> context.getColor(R.color.purple_secondary_200)
            "orange" -> context.getColor(R.color.orange_secondary_200)
            "pink" -> context.getColor(R.color.pink_secondary_200)
            else -> context.getColor(R.color.green_secondary_200)
        }
    }
    
    fun getSecondaryVariantColor(context: Context): Int {
        val colorTheme = ThemeUtils.getSavedColorTheme(context)
        return when (colorTheme) {
            "blue" -> context.getColor(R.color.blue_secondary_700)
            "red" -> context.getColor(R.color.red_secondary_700)
            "purple" -> context.getColor(R.color.purple_secondary_700)
            "orange" -> context.getColor(R.color.orange_secondary_700)
            "pink" -> context.getColor(R.color.pink_secondary_700)
            else -> context.getColor(R.color.green_secondary_700)
        }
    }
}

