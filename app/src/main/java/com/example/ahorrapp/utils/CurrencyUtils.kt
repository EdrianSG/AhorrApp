package com.example.ahorrapp.utils

import android.content.Context
import java.text.NumberFormat
import java.util.*

object CurrencyUtils {
    fun formatAmount(context: Context, amount: Double): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val currencyCode = prefs.getString("currency", "USD") ?: "USD"
        
        val format = NumberFormat.getCurrencyInstance()
        format.currency = Currency.getInstance(currencyCode)
        
        // Manejar casos especiales
        return when (currencyCode) {
            "PEN" -> "S/ ${String.format("%.2f", amount)}"
            else -> format.format(amount)
        }
    }

    fun getCurrencySymbol(context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val currencyCode = prefs.getString("currency", "USD") ?: "USD"
        
        return when (currencyCode) {
            "PEN" -> "S/"
            "USD" -> "$"
            "EUR" -> "€"
            "MXN" -> "$"
            "ARS" -> "$"
            else -> Currency.getInstance(currencyCode).symbol
        }
    }
} 