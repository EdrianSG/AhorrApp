package com.example.ahorrapp.model

data class Transaccion(
    val id: Long = 0,
    val descripcion: String,
    val monto: Double,
    val tipo: TipoTransaccion,
    val fecha: Long,
    val categoria: String
)

enum class TipoTransaccion {
    INGRESO,
    GASTO
} 