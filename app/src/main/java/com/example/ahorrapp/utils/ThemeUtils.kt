package com.example.ahorrapp.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.example.ahorrapp.R

object ThemeUtils {
    
    fun getColorResources(context: Context, colorTheme: String): ColorResources {
        return when (colorTheme) {
            "blue" -> ColorResources(
                primary200 = context.getColor(R.color.blue_primary_200),
                primary500 = context.getColor(R.color.blue_primary_500),
                primary700 = context.getColor(R.color.blue_primary_700),
                secondary200 = context.getColor(R.color.blue_secondary_200),
                secondary700 = context.getColor(R.color.blue_secondary_700)
            )
            "red" -> ColorResources(
                primary200 = context.getColor(R.color.red_primary_200),
                primary500 = context.getColor(R.color.red_primary_500),
                primary700 = context.getColor(R.color.red_primary_700),
                secondary200 = context.getColor(R.color.red_secondary_200),
                secondary700 = context.getColor(R.color.red_secondary_700)
            )
            "purple" -> ColorResources(
                primary200 = context.getColor(R.color.purple_primary_200),
                primary500 = context.getColor(R.color.purple_primary_500),
                primary700 = context.getColor(R.color.purple_primary_700),
                secondary200 = context.getColor(R.color.purple_secondary_200),
                secondary700 = context.getColor(R.color.purple_secondary_700)
            )
            "orange" -> ColorResources(
                primary200 = context.getColor(R.color.orange_primary_200),
                primary500 = context.getColor(R.color.orange_primary_500),
                primary700 = context.getColor(R.color.orange_primary_700),
                secondary200 = context.getColor(R.color.orange_secondary_200),
                secondary700 = context.getColor(R.color.orange_secondary_700)
            )
            "pink" -> ColorResources(
                primary200 = context.getColor(R.color.pink_primary_200),
                primary500 = context.getColor(R.color.pink_primary_500),
                primary700 = context.getColor(R.color.pink_primary_700),
                secondary200 = context.getColor(R.color.pink_secondary_200),
                secondary700 = context.getColor(R.color.pink_secondary_700)
            )
            else -> ColorResources( // green (default)
                primary200 = context.getColor(R.color.green_primary_200),
                primary500 = context.getColor(R.color.green_primary_500),
                primary700 = context.getColor(R.color.green_primary_700),
                secondary200 = context.getColor(R.color.green_secondary_200),
                secondary700 = context.getColor(R.color.green_secondary_700)
            )
        }
    }
    
    fun applyColorTheme(context: Context, colorTheme: String) {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("color_theme", colorTheme).apply()
    }
    
    fun getSavedColorTheme(context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getString("color_theme", "green") ?: "green"
    }
    
    fun getPrimaryColorResId(colorTheme: String): Int {
        return when (colorTheme) {
            "blue" -> R.color.blue_primary_500
            "red" -> R.color.red_primary_500
            "purple" -> R.color.purple_primary_500
            "orange" -> R.color.orange_primary_500
            "pink" -> R.color.pink_primary_500
            else -> R.color.green_primary_500
        }
    }
    
    fun getPrimaryVariantColorResId(colorTheme: String): Int {
        return when (colorTheme) {
            "blue" -> R.color.blue_primary_700
            "red" -> R.color.red_primary_700
            "purple" -> R.color.purple_primary_700
            "orange" -> R.color.orange_primary_700
            "pink" -> R.color.pink_primary_700
            else -> R.color.green_primary_700
        }
    }
    
    fun getPrimaryLightColorResId(colorTheme: String): Int {
        return when (colorTheme) {
            "blue" -> R.color.blue_primary_200
            "red" -> R.color.red_primary_200
            "purple" -> R.color.purple_primary_200
            "orange" -> R.color.orange_primary_200
            "pink" -> R.color.pink_primary_200
            else -> R.color.green_primary_200
        }
    }
    
    fun getSecondaryColorResId(colorTheme: String): Int {
        return when (colorTheme) {
            "blue" -> R.color.blue_secondary_200
            "red" -> R.color.red_secondary_200
            "purple" -> R.color.purple_secondary_200
            "orange" -> R.color.orange_secondary_200
            "pink" -> R.color.pink_secondary_200
            else -> R.color.green_secondary_200
        }
    }
    
    fun getSecondaryVariantColorResId(colorTheme: String): Int {
        return when (colorTheme) {
            "blue" -> R.color.blue_secondary_700
            "red" -> R.color.red_secondary_700
            "purple" -> R.color.purple_secondary_700
            "orange" -> R.color.orange_secondary_700
            "pink" -> R.color.pink_secondary_700
            else -> R.color.green_secondary_700
        }
    }
    
    fun isDarkMode(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and 
            Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
    
    data class ColorResources(
        val primary200: Int,
        val primary500: Int,
        val primary700: Int,
        val secondary200: Int,
        val secondary700: Int
    )
}

