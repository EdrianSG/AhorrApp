package com.example.ahorrapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class SummaryFragment : Fragment() {
    private lateinit var pieChart: PieChart
    private lateinit var totalIngresos: TextView
    private lateinit var totalGastos: TextView
    private lateinit var balanceTotal: TextView
    private lateinit var sessionManager: SessionManager

    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(
            TransactionRepository(AppDatabase.getDatabase(requireContext()).transactionDao()),
            sessionManager.getUserId()
        )
    }

    private val currencyChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.ahorrapp.CURRENCY_CHANGED") {
                viewModel.updateTotals()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Registrar el receptor de cambios de moneda
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(currencyChangeReceiver, IntentFilter("com.example.ahorrapp.CURRENCY_CHANGED"))
    }

    override fun onDestroy() {
        super.onDestroy()
        // Desregistrar el receptor de cambios de moneda
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(currencyChangeReceiver)
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

        pieChart = view.findViewById(R.id.pieChart)
        totalIngresos = view.findViewById(R.id.totalIngresos)
        totalGastos = view.findViewById(R.id.totalGastos)
        balanceTotal = view.findViewById(R.id.balanceTotal)

        setupPieChart()
        observeViewModel()
    }

    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(android.R.color.transparent)
            holeRadius = 58f
            setDrawEntryLabels(true)
            legend.isEnabled = true
            setEntryLabelTextSize(12f)
            setUsePercentValues(true)
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
            yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
            sliceSpace = 3f
        }

        val pieData = PieData(dataSet).apply {
            setValueTextSize(14f)
            setValueTextColor(Color.WHITE)
            setValueFormatter(PercentFormatter(pieChart))
        }

        pieChart.data = pieData
        pieChart.invalidate()
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateTotals()
    }
} 