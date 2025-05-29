package com.example.ahorrapp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.ahorrapp.data.AppDatabase
import com.example.ahorrapp.data.repository.TransactionRepository
import com.example.ahorrapp.utils.CurrencyUtils
import com.example.ahorrapp.utils.SessionManager
import com.example.ahorrapp.viewmodel.TransactionViewModel
import com.example.ahorrapp.viewmodel.TransactionViewModelFactory
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class SummaryFragment : Fragment() {
    private lateinit var pieChart: PieChart
    private lateinit var balanceTotal: TextView
    private lateinit var totalIngresos: TextView
    private lateinit var totalGastos: TextView
    private lateinit var sessionManager: SessionManager

    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(
            TransactionRepository(AppDatabase.getDatabase(requireContext()).transactionDao()),
            sessionManager.getUserId()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar vistas
        pieChart = view.findViewById(R.id.pieChart)
        balanceTotal = view.findViewById(R.id.balanceTotal)
        totalIngresos = view.findViewById(R.id.totalIngresos)
        totalGastos = view.findViewById(R.id.totalGastos)

        setupPieChart()
        observeViewModel()
    }

    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            setUsePercentValues(true)
            legend.isEnabled = true
        }
    }

    private fun observeViewModel() {
        viewModel.totalIngresos.observe(viewLifecycleOwner) { ingresos ->
            totalIngresos.text = CurrencyUtils.formatAmount(requireContext(), ingresos)
            updateBalance()
        }

        viewModel.totalGastos.observe(viewLifecycleOwner) { gastos ->
            totalGastos.text = CurrencyUtils.formatAmount(requireContext(), gastos)
            updateBalance()
        }

        viewModel.getCategoryTotals("GASTO").observe(viewLifecycleOwner) { categoryTotals ->
            updatePieChart(categoryTotals)
        }
    }

    private fun updateBalance() {
        val ingresos = viewModel.totalIngresos.value ?: 0.0
        val gastos = viewModel.totalGastos.value ?: 0.0
        val balance = ingresos - gastos
        balanceTotal.text = CurrencyUtils.formatAmount(requireContext(), balance)
    }

    private fun updatePieChart(categoryTotals: List<com.example.ahorrapp.data.model.CategoryTotal>) {
        if (categoryTotals.isEmpty()) {
            pieChart.setNoDataText("No hay datos disponibles")
            pieChart.invalidate()
            return
        }

        val entries = categoryTotals.map { categoryTotal ->
            PieEntry(categoryTotal.total.toFloat(), categoryTotal.category)
        }

        val dataSet = PieDataSet(entries, "Categorías").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 14f
            valueTextColor = Color.WHITE
            valueFormatter = PercentFormatter(pieChart)
        }

        val pieData = PieData(dataSet).apply {
            setValueTextSize(14f)
            setValueTextColor(Color.WHITE)
        }

        pieChart.data = pieData
        pieChart.invalidate()
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateTotals()
    }
} 