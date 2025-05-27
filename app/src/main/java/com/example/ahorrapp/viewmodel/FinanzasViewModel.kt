package com.example.ahorrapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ahorrapp.data.TransaccionRepository
import com.example.ahorrapp.model.Transaccion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FinanzasViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TransaccionRepository(application)

    private val _transacciones = MutableStateFlow<List<Transaccion>>(emptyList())
    val transacciones: StateFlow<List<Transaccion>> = _transacciones.asStateFlow()

    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            _transacciones.value = repository.obtenerTodasLasTransacciones()
            _balance.value = repository.obtenerBalance()
        }
    }

    fun agregarTransaccion(transaccion: Transaccion) {
        viewModelScope.launch {
            repository.insertarTransaccion(transaccion)
            cargarDatos()
        }
    }
} 