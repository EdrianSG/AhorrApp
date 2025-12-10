package com.example.ahorrapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.Spinner
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ahorrapp.adapter.CategoryAdapter
import com.example.ahorrapp.adapter.WalletWithBalanceAdapter
import com.example.ahorrapp.adapter.WalletWithBalance
import com.example.ahorrapp.data.AppDatabase
import com.example.ahorrapp.data.model.Categories
import com.example.ahorrapp.data.model.Transaction
import com.example.ahorrapp.data.model.TransactionCategory
import com.example.ahorrapp.data.model.TransactionType
import com.example.ahorrapp.data.repository.TransactionRepository
import com.example.ahorrapp.data.repository.CategoryLimitRepository
import com.example.ahorrapp.data.repository.WalletRepository
import com.example.ahorrapp.data.model.Wallet
import com.example.ahorrapp.utils.CurrencyUtils
import com.example.ahorrapp.utils.SessionManager
import com.example.ahorrapp.viewmodel.TransactionViewModel
import com.example.ahorrapp.viewmodel.TransactionViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.ahorrapp.data.repository.NotificationSettingsRepository
import com.example.ahorrapp.service.EnhancedNotificationService
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var balanceTextView: TextView
    private lateinit var ingresosTextView: TextView
    private lateinit var gastosTextView: TextView
    private var selectedCategory: TransactionCategory? = null

    private val viewModel: TransactionViewModel by lazy {
        TransactionViewModelFactory(
            TransactionRepository(AppDatabase.getDatabase(requireContext()).transactionDao()),
            CategoryLimitRepository(AppDatabase.getDatabase(requireContext()).categoryLimitDao()),
            EnhancedNotificationService(requireContext()),
            NotificationSettingsRepository(AppDatabase.getDatabase(requireContext()).notificationSettingsDao()),
            sessionManager.getUserId()
        ).create(TransactionViewModel::class.java)
    }

    private val currencyChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.ahorrapp.CURRENCY_CHANGED") {
                refreshData()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(requireContext())
        
        // Verificar si el usuario está autenticado
        if (!sessionManager.isLoggedIn()) {
            findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
            return
        }

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
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar vistas
        recyclerView = view.findViewById(R.id.transactionsList)
        balanceTextView = view.findViewById(R.id.balanceText)
        ingresosTextView = view.findViewById(R.id.ingresosText)
        gastosTextView = view.findViewById(R.id.gastosText)

        setupRecyclerView()
        setupFab(view)
        observeViewModel()

        // Inicializar valores por defecto
        updateBalance()
        
        // Forzar actualización de datos
        viewModel.updateTotals()
    }

    override fun onResume() {
        super.onResume()
        // Actualizar datos cuando el fragmento vuelve a estar visible
        refreshData()
    }

    fun refreshData() {
        viewModel.updateTotals()
        updateBalance()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(
            onEditClick = { transaction -> showEditTransactionDialog(transaction) },
            onDeleteClick = { transaction -> showDeleteConfirmationDialog(transaction) }
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun setupFab(view: View) {
        view.findViewById<FloatingActionButton>(R.id.addTransactionFab).setOnClickListener {
            showAddTransactionDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            adapter.submitList(transactions)
            viewModel.updateTotals()
        }

        viewModel.totalIngresos.observe(viewLifecycleOwner) { ingresos ->
            ingresosTextView.text = CurrencyUtils.formatAmount(requireContext(), ingresos ?: 0.0)
            updateBalance()
        }

        viewModel.totalGastos.observe(viewLifecycleOwner) { gastos ->
            gastosTextView.text = CurrencyUtils.formatAmount(requireContext(), gastos ?: 0.0)
            updateBalance()
        }

        viewModel.transactionResult.observe(viewLifecycleOwner) { result ->
            if (result == null) return@observe
            result.fold(
                onSuccess = {
                    Toast.makeText(context, "Transacción guardada exitosamente", Toast.LENGTH_SHORT).show()
                    viewModel.updateTotals()
                },
                onFailure = { exception ->
                    Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
            // Limpiar para evitar que el mensaje se repita al volver a la pestaña
            viewModel.clearTransactionResult()
        }
    }

    private fun updateBalance() {
        val ingresos = viewModel.totalIngresos.value ?: 0.0
        val gastos = viewModel.totalGastos.value ?: 0.0
        val balance = ingresos - gastos
        balanceTextView.text = CurrencyUtils.formatAmount(requireContext(), balance)
    }

    private fun showCategorySelector(
        dialogView: View,
        currentType: TransactionType,
        onCategorySelected: (TransactionCategory) -> Unit
    ) {
        val categories = Categories.getCategoriesByType(currentType)
        var tempSelectedCategory: TransactionCategory? = null
        val categoryAdapter = CategoryAdapter(categories) { category ->
            tempSelectedCategory = category
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Seleccionar Categoría")
            .setAdapter(categoryAdapter) { _, which ->
                tempSelectedCategory = categories[which]
            }
            .setPositiveButton("Aceptar") { dialog, _ ->
                tempSelectedCategory?.let { category ->
                    selectedCategory = category
                    dialogView.findViewById<MaterialButton>(R.id.categoryButton).text = category.name
                    dialogView.findViewById<ImageView>(R.id.categoryIcon).setImageResource(category.iconResourceId)
                    onCategorySelected(category)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAddTransactionDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_transaction, null)
        selectedCategory = null

        // Fecha y hora por defecto: ahora
        val calendar = Calendar.getInstance()
        val dateButton = dialogView.findViewById<MaterialButton>(R.id.dateButton)
        val timeButton = dialogView.findViewById<MaterialButton>(R.id.timeButton)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun updateDateTimeButtons() {
            dateButton.text = dateFormat.format(calendar.time)
            timeButton.text = timeFormat.format(calendar.time)
        }

        updateDateTimeButtons()

        val typeRadioGroup = dialogView.findViewById<RadioGroup>(R.id.typeRadioGroup)
        val categoryButton = dialogView.findViewById<MaterialButton>(R.id.categoryButton)
        val walletSpinner = dialogView.findViewById<Spinner>(R.id.walletSpinner)

        // Cargar billeteras con balance
        val walletDao = AppDatabase.getDatabase(requireContext()).walletDao()
        val walletRepository = WalletRepository(walletDao)
        val transactionDao = AppDatabase.getDatabase(requireContext()).transactionDao()
        val transactionRepository = TransactionRepository(transactionDao)
        var wallets: List<Wallet> = emptyList()

        viewLifecycleOwner.lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            wallets = walletRepository.getActiveWalletsByUserOnce(userId)

            if (wallets.isEmpty()) {
                // Crear billeteras por defecto
                val defaultWallets = listOf("Efectivo", "Tarjeta de Débito", "Yape", "Plin")
                defaultWallets.forEach { name ->
                    walletRepository.addWallet(Wallet(userId = userId, name = name))
                }
                wallets = walletRepository.getActiveWalletsByUserOnce(userId)
            }

            // Calcular balance para cada billetera
            val walletsWithBalance = wallets.map { wallet ->
                val balance = transactionRepository.getWalletBalance(userId, wallet.id)
                WalletWithBalance(wallet, balance)
            }

            val adapter = WalletWithBalanceAdapter(requireContext(), walletsWithBalance)
            walletSpinner.adapter = adapter
        }

        typeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val type = if (checkedId == R.id.incomeRadio) TransactionType.INGRESO else TransactionType.GASTO
            selectedCategory = null
            categoryButton.text = "Seleccionar Categoría"
            dialogView.findViewById<ImageView>(R.id.categoryIcon).setImageDrawable(null)
        }

        categoryButton.setOnClickListener {
            val type = if (dialogView.findViewById<RadioButton>(R.id.incomeRadio).isChecked) 
                TransactionType.INGRESO else TransactionType.GASTO
            showCategorySelector(dialogView, type) { }
        }

        dateButton.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateDateTimeButtons()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        timeButton.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    updateDateTimeButtons()
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nueva Transacción")
            .setView(dialogView)
            .setPositiveButton("Guardar") { dialog, _ ->
                val description = dialogView.findViewById<EditText>(R.id.descriptionInput).text.toString()
                val amount = dialogView.findViewById<EditText>(R.id.amountInput).text.toString().toDoubleOrNull()
                val isIncome = dialogView.findViewById<RadioButton>(R.id.incomeRadio).isChecked

                if (description.isNotEmpty() && amount != null && selectedCategory != null) {
                    val selectedWallet = if (walletSpinner.adapter != null && walletSpinner.selectedItemPosition >= 0) {
                        val adapter = walletSpinner.adapter as? WalletWithBalanceAdapter
                        adapter?.getItem(walletSpinner.selectedItemPosition)?.wallet
                    } else null

                    viewModel.addTransaction(
                        description = description,
                        amount = amount,
                        type = if (isIncome) "INGRESO" else "GASTO",
                        category = selectedCategory!!.name,
                        date = calendar.time,
                        walletId = selectedWallet?.id
                    )
                } else {
                    Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showEditTransactionDialog(transaction: Transaction) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_transaction, null)
        selectedCategory = Categories.getAllCategories().find { it.name == transaction.category }

        // Inicializar fecha y hora con la de la transacción
        val calendar = Calendar.getInstance().apply { time = transaction.date }
        val dateButton = dialogView.findViewById<MaterialButton>(R.id.dateButton)
        val timeButton = dialogView.findViewById<MaterialButton>(R.id.timeButton)
        val walletSpinner = dialogView.findViewById<Spinner>(R.id.walletSpinner)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun updateDateTimeButtons() {
            dateButton.text = dateFormat.format(calendar.time)
            timeButton.text = timeFormat.format(calendar.time)
        }

        updateDateTimeButtons()

        // Cargar billeteras con balance
        val walletDao = AppDatabase.getDatabase(requireContext()).walletDao()
        val walletRepository = WalletRepository(walletDao)
        val transactionDao = AppDatabase.getDatabase(requireContext()).transactionDao()
        val transactionRepository = TransactionRepository(transactionDao)
        var wallets: List<Wallet> = emptyList()

        viewLifecycleOwner.lifecycleScope.launch {
            val userId = sessionManager.getUserId()
            wallets = walletRepository.getActiveWalletsByUserOnce(userId)

            if (wallets.isEmpty()) {
                val defaultWallets = listOf("Efectivo", "Tarjeta de Débito", "Yape", "Plin")
                defaultWallets.forEach { name ->
                    walletRepository.addWallet(Wallet(userId = userId, name = name))
                }
                wallets = walletRepository.getActiveWalletsByUserOnce(userId)
            }

            // Calcular balance para cada billetera
            val walletsWithBalance = wallets.map { wallet ->
                val balance = transactionRepository.getWalletBalance(userId, wallet.id)
                WalletWithBalance(wallet, balance)
            }

            val adapter = WalletWithBalanceAdapter(requireContext(), walletsWithBalance)
            walletSpinner.adapter = adapter

            // Seleccionar billetera actual si existe
            transaction.walletId?.let { currentWalletId ->
                val index = walletsWithBalance.indexOfFirst { it.wallet.id == currentWalletId }
                if (index >= 0) {
                    walletSpinner.setSelection(index)
                }
            }
        }
        dialogView.findViewById<EditText>(R.id.descriptionInput).setText(transaction.description)
        dialogView.findViewById<EditText>(R.id.amountInput).setText(transaction.amount.toString())
        
        val categoryButton = dialogView.findViewById<MaterialButton>(R.id.categoryButton)
        val categoryIcon = dialogView.findViewById<ImageView>(R.id.categoryIcon)
        
        selectedCategory?.let {
            categoryButton.text = it.name
            categoryIcon.setImageResource(it.iconResourceId)
        }

        val typeRadioGroup = dialogView.findViewById<RadioGroup>(R.id.typeRadioGroup)
        if (transaction.type == "INGRESO") {
            dialogView.findViewById<RadioButton>(R.id.incomeRadio).isChecked = true
        } else {
            dialogView.findViewById<RadioButton>(R.id.expenseRadio).isChecked = true
        }

        typeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val type = if (checkedId == R.id.incomeRadio) TransactionType.INGRESO else TransactionType.GASTO
            selectedCategory = null
            categoryButton.text = "Seleccionar Categoría"
            categoryIcon.setImageDrawable(null)
        }

        categoryButton.setOnClickListener {
            val type = if (dialogView.findViewById<RadioButton>(R.id.incomeRadio).isChecked) 
                TransactionType.INGRESO else TransactionType.GASTO
            showCategorySelector(dialogView, type) { }
        }

        dateButton.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateDateTimeButtons()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        timeButton.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    updateDateTimeButtons()
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Editar Transacción")
            .setView(dialogView)
            .setPositiveButton("Guardar") { dialog, _ ->
                val description = dialogView.findViewById<EditText>(R.id.descriptionInput).text.toString()
                val amount = dialogView.findViewById<EditText>(R.id.amountInput).text.toString().toDoubleOrNull()
                val isIncome = dialogView.findViewById<RadioButton>(R.id.incomeRadio).isChecked

                if (description.isNotEmpty() && amount != null && selectedCategory != null) {
                    val selectedWallet = if (walletSpinner.adapter != null && walletSpinner.selectedItemPosition >= 0) {
                        val adapter = walletSpinner.adapter as? WalletWithBalanceAdapter
                        adapter?.getItem(walletSpinner.selectedItemPosition)?.wallet
                    } else null

                    val updatedTransaction = transaction.copy(
                        description = description,
                        amount = amount,
                        type = if (isIncome) "INGRESO" else "GASTO",
                        category = selectedCategory!!.name,
                        date = calendar.time,
                        walletId = selectedWallet?.id
                    )
                    viewModel.updateTransaction(updatedTransaction)
                } else {
                    Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDeleteConfirmationDialog(transaction: Transaction) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar Transacción")
            .setMessage("¿Estás seguro de que deseas eliminar esta transacción?")
            .setPositiveButton("Eliminar") { dialog, _ ->
                viewModel.deleteTransaction(transaction)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}