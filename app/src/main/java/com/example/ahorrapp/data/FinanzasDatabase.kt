package com.example.ahorrapp.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log

object FinanzasContract {
    object TransaccionEntry : BaseColumns {
        const val TABLE_NAME = "transacciones"
        const val COLUMN_DESCRIPCION = "descripcion"
        const val COLUMN_MONTO = "monto"
        const val COLUMN_TIPO = "tipo"
        const val COLUMN_FECHA = "fecha"
        const val COLUMN_CATEGORIA = "categoria"
    }
}

class FinanzasDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Finanzas.db"

        private const val SQL_CREATE_ENTRIES = """
            CREATE TABLE IF NOT EXISTS ${FinanzasContract.TransaccionEntry.TABLE_NAME} (
                ${BaseColumns._ID} INTEGER PRIMARY KEY,
                ${FinanzasContract.TransaccionEntry.COLUMN_DESCRIPCION} TEXT,
                ${FinanzasContract.TransaccionEntry.COLUMN_MONTO} REAL,
                ${FinanzasContract.TransaccionEntry.COLUMN_TIPO} TEXT,
                ${FinanzasContract.TransaccionEntry.COLUMN_FECHA} INTEGER,
                ${FinanzasContract.TransaccionEntry.COLUMN_CATEGORIA} TEXT
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // COMENTADO PARA EVITAR PÉRDIDA DE DATOS EN BETA
        // db.execSQL("DROP TABLE IF EXISTS ${FinanzasContract.TransaccionEntry.TABLE_NAME}")
        // onCreate(db)
        Log.w("FinanzasDatabase", "onUpgrade llamado de $oldVersion a $newVersion. No se borraron datos.")
    }
}
