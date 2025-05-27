package com.example.ahorrapp

import java.util.Date

data class Transaction(
    val description: String,
    val amount: Double,
    val type: String, // "Ingreso" o "Gasto"
    val category: String,
    val date: Date = Date()
) 