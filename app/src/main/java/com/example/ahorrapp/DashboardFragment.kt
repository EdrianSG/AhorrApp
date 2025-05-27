package com.example.ahorrapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.ahorrapp.data.FinanzasContract
import com.example.ahorrapp.data.FinanzasDatabase
import com.example.ahorrapp.utils.CurrencyUtils
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.util.*

class DashboardFragment : Fragment() {
    private lateinit var dbHelper: FinanzasDatabase
    private lateinit var pieChart: PieChart
    private lateinit var totalIngresos: TextView
    private lateinit var totalGastos: TextView
    private lateinit var balanceTotal: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        dbHelper = FinanzasDatabase(requireContext())
        
        // Inicializar vistas
        pieChart = view.findViewById(R.id.pieChart)
        totalIngresos = view.findViewById(R.id.totalIngresos)
        totalGastos = view.findViewById(R.id.totalGastos)
        balanceTotal = view.findViewById(R.id.balanceTotal)

        setupPieChart()
        loadData()
    }

    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(android.R.color.transparent)
            holeRadius = 58f
            setDrawEntryLabels(false)
            legend.isEnabled = true
            setEntryLabelTextSize(12f)
        }
    }

    private fun loadData() {
        val db = dbHelper.readableDatabase
        var ingresos = 0.0
        var gastos = 0.0
        val gastosPorCategoria = mutableMapOf<String, Double>()

        // Consultar todas las transacciones
        val cursor = db.query(
            FinanzasContract.TransaccionEntry.TABLE_NAME,
            arrayOf(
                FinanzasContract.TransaccionEntry.COLUMN_MONTO,
                FinanzasContract.TransaccionEntry.COLUMN_TIPO,
                FinanzasContract.TransaccionEntry.COLUMN_CATEGORIA
            ),
            null,
            null,
            null,
            null,
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                val monto = it.getDouble(it.getColumnIndexOrThrow(FinanzasContract.TransaccionEntry.COLUMN_MONTO))
                val tipo = it.getString(it.getColumnIndexOrThrow(FinanzasContract.TransaccionEntry.COLUMN_TIPO))
                val categoria = it.getString(it.getColumnIndexOrThrow(FinanzasContract.TransaccionEntry.COLUMN_CATEGORIA))

                if (tipo == "Ingreso") {
                    ingresos += monto
                } else {
                    gastos += monto
                    gastosPorCategoria[categoria] = (gastosPorCategoria[categoria] ?: 0.0) + monto
                }
            }
        }

        // Actualizar textos
        totalIngresos.text = CurrencyUtils.formatAmount(requireContext(), ingresos)
        totalGastos.text = CurrencyUtils.formatAmount(requireContext(), gastos)
        balanceTotal.text = CurrencyUtils.formatAmount(requireContext(), ingresos - gastos)

        // Actualizar gráfico
        updatePieChart(gastosPorCategoria)
    }

    private fun updatePieChart(gastosPorCategoria: Map<String, Double>) {
        val entries = gastosPorCategoria.map { (categoria, monto) ->
            PieEntry(monto.toFloat(), categoria)
        }

        val dataSet = PieDataSet(entries, "Gastos por Categoría").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 14f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return String.format("%.1f%%", value)
                }
            }
        }

        pieChart.data = PieData(dataSet)
        pieChart.invalidate()
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
} 