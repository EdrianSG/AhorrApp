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
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ahorrapp.data.AppDatabase
import com.example.ahorrapp.data.model.Transaction
import com.example.ahorrapp.data.repository.TransactionRepository
import com.example.ahorrapp.utils.CurrencyUtils
import com.example.ahorrapp.utils.SessionManager
import com.example.ahorrapp.viewmodel.TransactionViewModel
import com.example.ahorrapp.viewmodel.TransactionViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var balanceTextView: TextView
    private lateinit var ingresosTextView: TextView
    private lateinit var gastosTextView: TextView

    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(
            TransactionRepository(AppDatabase.getDatabase(requireContext()).transactionDao()),
            sessionManager.getUserId()
        )
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
            result.fold(
                onSuccess = {
                    Toast.makeText(context, "Transacción guardada exitosamente", Toast.LENGTH_SHORT).show()
                    viewModel.updateTotals()
                },
                onFailure = { exception ->
                    Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun updateBalance() {
        val ingresos = viewModel.totalIngresos.value ?: 0.0
        val gastos = viewModel.totalGastos.value ?: 0.0
        val balance = ingresos - gastos
        balanceTextView.text = CurrencyUtils.formatAmount(requireContext(), balance)
    }

    private fun showAddTransactionDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_transaction, null)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nueva Transacción")
            .setView(dialogView)
            .setPositiveButton("Guardar") { dialog, _ ->
                val description = dialogView.findViewById<EditText>(R.id.descriptionInput).text.toString()
                val amount = dialogView.findViewById<EditText>(R.id.amountInput).text.toString().toDoubleOrNull()
                val category = dialogView.findViewById<EditText>(R.id.categoryInput).text.toString()
                val isIncome = dialogView.findViewById<RadioButton>(R.id.incomeRadio).isChecked

                if (description.isNotEmpty() && amount != null && category.isNotEmpty()) {
                    viewModel.addTransaction(
                        description = description,
                        amount = amount,
                        type = if (isIncome) "INGRESO" else "GASTO",
                        category = category
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
        
        dialogView.findViewById<EditText>(R.id.descriptionInput).setText(transaction.description)
        dialogView.findViewById<EditText>(R.id.amountInput).setText(transaction.amount.toString())
        dialogView.findViewById<EditText>(R.id.categoryInput).setText(transaction.category)
        
        if (transaction.type == "INGRESO") {
            dialogView.findViewById<RadioButton>(R.id.incomeRadio).isChecked = true
        } else {
            dialogView.findViewById<RadioButton>(R.id.expenseRadio).isChecked = true
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Editar Transacción")
            .setView(dialogView)
            .setPositiveButton("Guardar") { dialog, _ ->
                val description = dialogView.findViewById<EditText>(R.id.descriptionInput).text.toString()
                val amount = dialogView.findViewById<EditText>(R.id.amountInput).text.toString().toDoubleOrNull()
                val category = dialogView.findViewById<EditText>(R.id.categoryInput).text.toString()
                val isIncome = dialogView.findViewById<RadioButton>(R.id.incomeRadio).isChecked

                if (description.isNotEmpty() && amount != null && category.isNotEmpty()) {
                    val updatedTransaction = transaction.copy(
                        description = description,
                        amount = amount,
                        type = if (isIncome) "INGRESO" else "GASTO",
                        category = category
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