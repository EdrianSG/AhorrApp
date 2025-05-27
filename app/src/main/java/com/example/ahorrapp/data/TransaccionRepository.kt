package com.example.ahorrapp.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import com.example.ahorrapp.model.Transaccion
import com.example.ahorrapp.model.TipoTransaccion

class TransaccionRepository(context: Context) {
    private val database = FinanzasDatabase(context)

    fun insertarTransaccion(transaccion: Transaccion): Long {
        val db = database.writableDatabase

        val values = ContentValues().apply {
            put(FinanzasContract.TransaccionEntry.COLUMN_DESCRIPCION, transaccion.descripcion)
            put(FinanzasContract.TransaccionEntry.COLUMN_MONTO, transaccion.monto)
            put(FinanzasContract.TransaccionEntry.COLUMN_TIPO, transaccion.tipo.name)
            put(FinanzasContract.TransaccionEntry.COLUMN_FECHA, transaccion.fecha)
            put(FinanzasContract.TransaccionEntry.COLUMN_CATEGORIA, transaccion.categoria)
        }

        return db.insert(FinanzasContract.TransaccionEntry.TABLE_NAME, null, values)
    }

    fun obtenerTodasLasTransacciones(): List<Transaccion> {
        val db = database.readableDatabase
        
        val projection = arrayOf(
            BaseColumns._ID,
            FinanzasContract.TransaccionEntry.COLUMN_DESCRIPCION,
            FinanzasContract.TransaccionEntry.COLUMN_MONTO,
            FinanzasContract.TransaccionEntry.COLUMN_TIPO,
            FinanzasContract.TransaccionEntry.COLUMN_FECHA,
            FinanzasContract.TransaccionEntry.COLUMN_CATEGORIA
        )

        val cursor = db.query(
            FinanzasContract.TransaccionEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            "${FinanzasContract.TransaccionEntry.COLUMN_FECHA} DESC"
        )

        return generarListaDeTransacciones(cursor)
    }

    private fun generarListaDeTransacciones(cursor: Cursor): List<Transaccion> {
        val transacciones = mutableListOf<Transaccion>()

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(BaseColumns._ID))
                val descripcion = getString(getColumnIndexOrThrow(FinanzasContract.TransaccionEntry.COLUMN_DESCRIPCION))
                val monto = getDouble(getColumnIndexOrThrow(FinanzasContract.TransaccionEntry.COLUMN_MONTO))
                val tipo = TipoTransaccion.valueOf(getString(getColumnIndexOrThrow(FinanzasContract.TransaccionEntry.COLUMN_TIPO)))
                val fecha = getLong(getColumnIndexOrThrow(FinanzasContract.TransaccionEntry.COLUMN_FECHA))
                val categoria = getString(getColumnIndexOrThrow(FinanzasContract.TransaccionEntry.COLUMN_CATEGORIA))

                transacciones.add(Transaccion(id, descripcion, monto, tipo, fecha, categoria))
            }
        }
        cursor.close()
        return transacciones
    }

    fun obtenerBalance(): Double {
        val transacciones = obtenerTodasLasTransacciones()
        return transacciones.sumOf { 
            when (it.tipo) {
                TipoTransaccion.INGRESO -> it.monto
                TipoTransaccion.GASTO -> -it.monto
            }
        }
    }
} 