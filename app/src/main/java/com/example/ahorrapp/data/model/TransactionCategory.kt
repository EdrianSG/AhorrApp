package com.example.ahorrapp.data.model

import com.example.ahorrapp.R

enum class TransactionType {
    INGRESO,
    GASTO
}

data class TransactionCategory(
    val id: String,
    val name: String,
    val type: TransactionType,
    val iconResourceId: Int
)

object Categories {
    val INCOME_CATEGORIES = listOf(
        TransactionCategory("salary", "Salario", TransactionType.INGRESO, R.drawable.ic_salary),
        TransactionCategory("investment", "Inversiones", TransactionType.INGRESO, R.drawable.ic_investment),
        TransactionCategory("freelance", "Trabajo Freelance", TransactionType.INGRESO, R.drawable.ic_freelance),
        TransactionCategory("bonus", "Bonos", TransactionType.INGRESO, R.drawable.ic_bonus),
        TransactionCategory("rent", "Alquiler", TransactionType.INGRESO, R.drawable.ic_rent),
        TransactionCategory("other_income", "Otros Ingresos", TransactionType.INGRESO, R.drawable.ic_other)
    )

    val EXPENSE_CATEGORIES = listOf(
        TransactionCategory("food", "Alimentación", TransactionType.GASTO, R.drawable.ic_food),
        TransactionCategory("transport", "Transporte", TransactionType.GASTO, R.drawable.ic_transport),
        TransactionCategory("housing", "Vivienda", TransactionType.GASTO, R.drawable.ic_housing),
        TransactionCategory("utilities", "Servicios", TransactionType.GASTO, R.drawable.ic_utilities),
        TransactionCategory("entertainment", "Entretenimiento", TransactionType.GASTO, R.drawable.ic_entertainment),
        TransactionCategory("health", "Salud", TransactionType.GASTO, R.drawable.ic_health),
        TransactionCategory("education", "Educación", TransactionType.GASTO, R.drawable.ic_education),
        TransactionCategory("shopping", "Compras", TransactionType.GASTO, R.drawable.ic_shopping),
        TransactionCategory("other_expense", "Otros Gastos", TransactionType.GASTO, R.drawable.ic_other)
    )

    fun getAllCategories() = INCOME_CATEGORIES + EXPENSE_CATEGORIES

    fun getCategoriesByType(type: TransactionType) = when (type) {
        TransactionType.INGRESO -> INCOME_CATEGORIES
        TransactionType.GASTO -> EXPENSE_CATEGORIES
    }

    fun getCategoryById(id: String) = getAllCategories().find { it.id == id }
} 