package com.example.ahorrapp.utils

import android.content.Context
import java.text.NumberFormat
import java.util.*

object CurrencyUtils {
    fun formatAmount(context: Context, amount: Double): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val currencyCode = prefs.getString("currency", "MXN") ?: "MXN"
        
        if (currencyCode == "PEN") {
            // Formato especial para soles peruanos
            return String.format("S/ %.2f", amount)
        }
        
        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        format.currency = Currency.getInstance(currencyCode)
        return format.format(amount)
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