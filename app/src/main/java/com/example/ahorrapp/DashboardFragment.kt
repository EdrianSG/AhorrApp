package com.example.ahorrapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.ahorrapp.utils.CurrencyUtils
import com.example.ahorrapp.viewmodel.TransactionViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    
    private val viewModel: TransactionViewModel by viewModels()
    
    private lateinit var pieChart: PieChart
    private lateinit var totalIngresosText: TextView
    private lateinit var totalGastosText: TextView
    private lateinit var balanceTotalText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Inicializar vistas
        pieChart = view.findViewById(R.id.pieChart)
        totalIngresosText = view.findViewById(R.id.totalIngresos)
        totalGastosText = view.findViewById(R.id.totalGastos)
        balanceTotalText = view.findViewById(R.id.balanceTotal)

        setupPieChart()
        setupObservers()
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
            // Texto central opcional
            setCenterText("Gastos")
            setCenterTextSize(16f)
        }
    }

    private fun setupObservers() {
        // Observar totales para los textos superiores
        viewModel.totalIngresos.observe(viewLifecycleOwner) { ingresos ->
            totalIngresosText.text = CurrencyUtils.formatAmount(requireContext(), ingresos ?: 0.0)
            updateBalance()
        }

        viewModel.totalGastos.observe(viewLifecycleOwner) { gastos ->
            totalGastosText.text = CurrencyUtils.formatAmount(requireContext(), gastos ?: 0.0)
            updateBalance()
        }

        // Observar gastos por categoría para el gráfico
        viewModel.getCategoryTotals("GASTO").observe(viewLifecycleOwner) { categoryTotals ->
            if (categoryTotals != null) {
                updatePieChart(categoryTotals.associate { it.category to it.total })
            }
        }
    }

    private fun updateBalance() {
        val ingresos = viewModel.totalIngresos.value ?: 0.0
        val gastos = viewModel.totalGastos.value ?: 0.0
        balanceTotalText.text = CurrencyUtils.formatAmount(requireContext(), ingresos - gastos)
    }

    private fun updatePieChart(gastosPorCategoria: Map<String, Double>) {
        if (gastosPorCategoria.isEmpty()) {
            pieChart.clear()
            pieChart.setNoDataText("No hay gastos registrados")
            return
        }

        val entries = gastosPorCategoria.map { (categoria, monto) ->
            PieEntry(monto.toFloat(), categoria)
        }

        val dataSet = PieDataSet(entries, "Gastos por Categoría").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 14f
            valueTextColor = android.graphics.Color.WHITE
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return String.format("%.1f%%", value)
                }
            }
        }

        pieChart.data = PieData(dataSet)
        pieChart.animateY(1000) // Animación suave al cargar
        pieChart.invalidate()
    }

    override fun onResume() {
        super.onResume()
        // Asegurarnos de que los totales estén frescos al volver a la pantalla
        viewModel.updateTotals()
    }
}
