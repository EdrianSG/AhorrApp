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
import com.example.ahorrapp.data.model.Transaction
import com.example.ahorrapp.data.model.CategoryTotal
import com.example.ahorrapp.data.repository.TransactionRepository
import com.example.ahorrapp.data.repository.CategoryLimitRepository
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
import com.example.ahorrapp.data.repository.NotificationSettingsRepository
import com.example.ahorrapp.service.EnhancedNotificationService
import android.widget.RadioGroup
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SummaryFragment : Fragment() {
    private lateinit var pieChartGastos: PieChart
    private lateinit var pieChartIngresos: PieChart
    private lateinit var totalIngresos: TextView
    private lateinit var totalGastos: TextView
    private lateinit var balanceTotal: TextView
    private lateinit var periodLabel: TextView
    private lateinit var prevPeriodButton: MaterialButton
    private lateinit var nextPeriodButton: MaterialButton
    private lateinit var periodTypeGroup: RadioGroup
    private lateinit var sessionManager: SessionManager

    private enum class PeriodType { WEEK, MONTH, YEAR }
    private var currentPeriodType: PeriodType = PeriodType.MONTH
    private val periodCalendar: Calendar = Calendar.getInstance()

    private var transactionsLiveData: androidx.lifecycle.LiveData<List<Transaction>>? = null

    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(
            TransactionRepository(AppDatabase.getDatabase(requireContext()).transactionDao()),
            CategoryLimitRepository(AppDatabase.getDatabase(requireContext()).categoryLimitDao()),
            EnhancedNotificationService(requireContext()),
            NotificationSettingsRepository(AppDatabase.getDatabase(requireContext()).notificationSettingsDao()),
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

        pieChartGastos = view.findViewById(R.id.pieChart)
        pieChartIngresos = view.findViewById(R.id.pieChartIngresos)
        totalIngresos = view.findViewById(R.id.totalIngresos)
        totalGastos = view.findViewById(R.id.totalGastos)
        balanceTotal = view.findViewById(R.id.balanceTotal)
        periodLabel = view.findViewById(R.id.periodLabel)
        prevPeriodButton = view.findViewById(R.id.prevPeriodButton)
        nextPeriodButton = view.findViewById(R.id.nextPeriodButton)
        periodTypeGroup = view.findViewById(R.id.periodTypeGroup)

        setupPieCharts()
        setupPeriodControls()
        loadDataForCurrentPeriod()
    }

    private fun setupPieCharts() {
        setupPieChart(pieChartGastos)
        setupPieChart(pieChartIngresos)
    }

    private fun setupPieChart(pieChart: PieChart) {
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

    private fun setupPeriodControls() {
        // Tipo de periodo
        periodTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            currentPeriodType = when (checkedId) {
                R.id.weekRadio -> PeriodType.WEEK
                R.id.yearRadio -> PeriodType.YEAR
                else -> PeriodType.MONTH
            }
            // Reiniciar al periodo actual
            periodCalendar.time = Date()
            loadDataForCurrentPeriod()
        }

        // Botones anterior / siguiente
        prevPeriodButton.setOnClickListener {
            shiftPeriod(-1)
            loadDataForCurrentPeriod()
        }

        nextPeriodButton.setOnClickListener {
            shiftPeriod(1)
            loadDataForCurrentPeriod()
        }
    }

    private fun shiftPeriod(offset: Int) {
        when (currentPeriodType) {
            PeriodType.WEEK -> periodCalendar.add(Calendar.WEEK_OF_YEAR, offset)
            PeriodType.MONTH -> periodCalendar.add(Calendar.MONTH, offset)
            PeriodType.YEAR -> periodCalendar.add(Calendar.YEAR, offset)
        }
    }

    private fun getCurrentPeriodRange(): Pair<Date, Date> {
        val calStart = periodCalendar.clone() as Calendar
        val calEnd = periodCalendar.clone() as Calendar

        when (currentPeriodType) {
            PeriodType.WEEK -> {
                calStart.firstDayOfWeek = Calendar.MONDAY
                calStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calEnd.firstDayOfWeek = Calendar.MONDAY
                calEnd.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            }
            PeriodType.MONTH -> {
                calStart.set(Calendar.DAY_OF_MONTH, 1)
                calEnd.set(Calendar.DAY_OF_MONTH, calEnd.getActualMaximum(Calendar.DAY_OF_MONTH))
            }
            PeriodType.YEAR -> {
                calStart.set(Calendar.DAY_OF_YEAR, 1)
                calEnd.set(Calendar.DAY_OF_YEAR, calEnd.getActualMaximum(Calendar.DAY_OF_YEAR))
            }
        }

        // Inicio del día
        calStart.set(Calendar.HOUR_OF_DAY, 0)
        calStart.set(Calendar.MINUTE, 0)
        calStart.set(Calendar.SECOND, 0)
        calStart.set(Calendar.MILLISECOND, 0)

        // Fin del día
        calEnd.set(Calendar.HOUR_OF_DAY, 23)
        calEnd.set(Calendar.MINUTE, 59)
        calEnd.set(Calendar.SECOND, 59)
        calEnd.set(Calendar.MILLISECOND, 999)

        return calStart.time to calEnd.time
    }

    private fun updatePeriodLabel(start: Date, end: Date) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

        val text = when (currentPeriodType) {
            PeriodType.WEEK -> "Semana: ${dateFormat.format(start)} - ${dateFormat.format(end)}"
            PeriodType.MONTH -> "Mes: ${monthFormat.format(start)}"
            PeriodType.YEAR -> "Año: ${yearFormat.format(start)}"
        }
        periodLabel.text = text.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    private fun loadDataForCurrentPeriod() {
        val (start, end) = getCurrentPeriodRange()
        updatePeriodLabel(start, end)

        // Quitar observadores anteriores
        transactionsLiveData?.removeObservers(viewLifecycleOwner)

        transactionsLiveData = viewModel.getTransactionsByPeriod(start, end)
        transactionsLiveData?.observe(viewLifecycleOwner) { transactions ->
            updateSummaryForTransactions(transactions)
        }
    }

    private fun updateSummaryForTransactions(transactions: List<Transaction>) {
        val ingresos = transactions.filter { it.type == "INGRESO" }
        val gastos = transactions.filter { it.type == "GASTO" }

        val totalIngresosValor = ingresos.sumOf { it.amount }
        val totalGastosValor = gastos.sumOf { it.amount }
        val balance = totalIngresosValor - totalGastosValor

        totalIngresos.text = CurrencyUtils.formatAmount(requireContext(), totalIngresosValor)
        totalGastos.text = CurrencyUtils.formatAmount(requireContext(), totalGastosValor)
        balanceTotal.text = CurrencyUtils.formatAmount(requireContext(), balance)

        // Agrupar por categoría
        val gastosPorCategoria: List<CategoryTotal> =
            gastos.groupBy { it.category }.map { (categoria, lista) ->
                CategoryTotal(category = categoria, total = lista.sumOf { it.amount })
            }
        val ingresosPorCategoria: List<CategoryTotal> =
            ingresos.groupBy { it.category }.map { (categoria, lista) ->
                CategoryTotal(category = categoria, total = lista.sumOf { it.amount })
            }

        updatePieChart(pieChartGastos, gastosPorCategoria, "Gastos por Categoría")
        updatePieChart(pieChartIngresos, ingresosPorCategoria, "Ingresos por Categoría")
    }

    private fun updatePieChart(
        pieChart: PieChart,
        categoryTotals: List<com.example.ahorrapp.data.model.CategoryTotal>,
        label: String
    ) {
        if (categoryTotals.isEmpty()) {
            pieChart.setNoDataText("No hay datos disponibles")
            pieChart.invalidate()
            return
        }

        val entries = categoryTotals.map { categoryTotal ->
            PieEntry(categoryTotal.total.toFloat(), categoryTotal.category)
        }

        val dataSet = PieDataSet(entries, label).apply {
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
        // Cuando volvemos, recargamos el periodo actual
        loadDataForCurrentPeriod()
    }
} 