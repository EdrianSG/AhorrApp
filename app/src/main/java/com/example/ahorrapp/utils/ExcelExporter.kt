package com.example.ahorrapp.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import com.example.ahorrapp.data.model.Transaction
import com.example.ahorrapp.data.model.Wallet
import com.example.ahorrapp.data.model.CategoryTotal
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class ExcelExporter(private val context: Context) {

    fun exportTransactions(
        transactions: List<Transaction>,
        fileName: String = "transacciones_${getCurrentDate()}.csv"
    ): File? {
        return try {
            val file = getFile(fileName)
            FileWriter(file).use { writer ->
                // Encabezados
                writer.appendLine("Fecha,Tipo,Categoría,Descripción,Monto,Billetera")
                
                // Datos
                transactions.forEach { transaction ->
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val date = dateFormat.format(transaction.date)
                    val type = escapeCsv(transaction.type)
                    val category = escapeCsv(transaction.category)
                    val description = escapeCsv(transaction.description)
                    val amount = transaction.amount.toString()
                    val wallet = transaction.walletId?.toString() ?: "N/A"
                    
                    writer.appendLine("$date,$type,$category,$description,$amount,$wallet")
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportWallets(
        wallets: List<Wallet>,
        walletBalances: Map<Long, Double>,
        fileName: String = "billeteras_${getCurrentDate()}.csv"
    ): File? {
        return try {
            val file = getFile(fileName)
            FileWriter(file).use { writer ->
                // Encabezados
                writer.appendLine("ID,Nombre,Balance,Estado")
                
                // Datos
                wallets.forEach { wallet ->
                    val balance = walletBalances[wallet.id] ?: 0.0
                    val estado = if (wallet.isActive) "Activa" else "Inactiva"
                    writer.appendLine("${wallet.id},${escapeCsv(wallet.name)},$balance,$estado")
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportCharts(
        gastosPorCategoria: List<CategoryTotal>,
        ingresosPorCategoria: List<CategoryTotal>,
        fileName: String = "graficos_${getCurrentDate()}.csv"
    ): File? {
        return try {
            val file = getFile(fileName)
            FileWriter(file).use { writer ->
                // Hoja de Gastos
                writer.appendLine("GASTOS POR CATEGORÍA")
                writer.appendLine("Categoría,Monto,Porcentaje")
                
                val totalGastos = gastosPorCategoria.sumOf { it.total }
                gastosPorCategoria.forEach { categoryTotal ->
                    val percentage = if (totalGastos > 0) {
                        String.format(Locale.getDefault(), "%.2f%%", (categoryTotal.total / totalGastos) * 100)
                    } else {
                        "0.00%"
                    }
                    writer.appendLine("${escapeCsv(categoryTotal.category)},${categoryTotal.total},$percentage")
                }
                
                writer.appendLine("")
                writer.appendLine("INGRESOS POR CATEGORÍA")
                writer.appendLine("Categoría,Monto,Porcentaje")
                
                val totalIngresos = ingresosPorCategoria.sumOf { it.total }
                ingresosPorCategoria.forEach { categoryTotal ->
                    val percentage = if (totalIngresos > 0) {
                        String.format(Locale.getDefault(), "%.2f%%", (categoryTotal.total / totalIngresos) * 100)
                    } else {
                        "0.00%"
                    }
                    writer.appendLine("${escapeCsv(categoryTotal.category)},${categoryTotal.total},$percentage")
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportAll(
        transactions: List<Transaction>,
        wallets: List<Wallet>,
        walletBalances: Map<Long, Double>,
        gastosPorCategoria: List<CategoryTotal>,
        ingresosPorCategoria: List<CategoryTotal>,
        fileName: String = "resumen_completo_${getCurrentDate()}.csv"
    ): File? {
        return try {
            val file = getFile(fileName)
            FileWriter(file).use { writer ->
                // Resumen
                writer.appendLine("RESUMEN FINANCIERO")
                writer.appendLine("")
                
                val ingresos = transactions.filter { it.type == "INGRESO" }
                val gastos = transactions.filter { it.type == "GASTO" }
                val totalIngresos = ingresos.sumOf { it.amount }
                val totalGastos = gastos.sumOf { it.amount }
                val balanceTotal = totalIngresos - totalGastos
                
                writer.appendLine("Total Ingresos,$totalIngresos")
                writer.appendLine("Total Gastos,$totalGastos")
                writer.appendLine("Balance,$balanceTotal")
                writer.appendLine("")
                
                // Transacciones
                writer.appendLine("TRANSACCIONES")
                writer.appendLine("Fecha,Tipo,Categoría,Descripción,Monto,Billetera")
                transactions.forEach { transaction ->
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val date = dateFormat.format(transaction.date)
                    val type = escapeCsv(transaction.type)
                    val category = escapeCsv(transaction.category)
                    val description = escapeCsv(transaction.description)
                    val amount = transaction.amount.toString()
                    val wallet = transaction.walletId?.toString() ?: "N/A"
                    
                    writer.appendLine("$date,$type,$category,$description,$amount,$wallet")
                }
                writer.appendLine("")
                
                // Billeteras
                writer.appendLine("BILLETERAS")
                writer.appendLine("ID,Nombre,Balance,Estado")
                wallets.forEach { wallet ->
                    val balance = walletBalances[wallet.id] ?: 0.0
                    val estado = if (wallet.isActive) "Activa" else "Inactiva"
                    writer.appendLine("${wallet.id},${escapeCsv(wallet.name)},$balance,$estado")
                }
                writer.appendLine("")
                
                // Gastos por Categoría
                writer.appendLine("GASTOS POR CATEGORÍA")
                writer.appendLine("Categoría,Monto,Porcentaje")
                val totalGastosChart = gastosPorCategoria.sumOf { it.total }
                gastosPorCategoria.forEach { categoryTotal ->
                    val percentage = if (totalGastosChart > 0) {
                        String.format(Locale.getDefault(), "%.2f%%", (categoryTotal.total / totalGastosChart) * 100)
                    } else {
                        "0.00%"
                    }
                    writer.appendLine("${escapeCsv(categoryTotal.category)},${categoryTotal.total},$percentage")
                }
                writer.appendLine("")
                
                // Ingresos por Categoría
                writer.appendLine("INGRESOS POR CATEGORÍA")
                writer.appendLine("Categoría,Monto,Porcentaje")
                val totalIngresosChart = ingresosPorCategoria.sumOf { it.total }
                ingresosPorCategoria.forEach { categoryTotal ->
                    val percentage = if (totalIngresosChart > 0) {
                        String.format(Locale.getDefault(), "%.2f%%", (categoryTotal.total / totalIngresosChart) * 100)
                    } else {
                        "0.00%"
                    }
                    writer.appendLine("${escapeCsv(categoryTotal.category)},${categoryTotal.total},$percentage")
                }
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun escapeCsv(value: String): String {
        // Si contiene comas, comillas o saltos de línea, envolver en comillas y escapar comillas internas
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    private fun getFile(fileName: String): File {
        val downloadsDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        } else {
            File(Environment.getExternalStorageDirectory(), "Download")
        }
        
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        
        return File(downloadsDir, fileName)
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
