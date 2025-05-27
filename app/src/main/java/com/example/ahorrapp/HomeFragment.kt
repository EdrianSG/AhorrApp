package com.example.ahorrapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*
import com.example.ahorrapp.data.FinanzasDatabase
import android.content.ContentValues
import android.provider.BaseColumns
import com.example.ahorrapp.data.FinanzasContract
import com.example.ahorrapp.utils.CurrencyUtils
import android.view.MenuInflater
import android.widget.PopupMenu
import androidx.appcompat.app.AlertDialog

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private lateinit var dbHelper: FinanzasDatabase
    private var totalBalance: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = FinanzasDatabase(requireContext())
        
        recyclerView = view.findViewById(R.id.transactionsList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = TransactionAdapter(mutableListOf(), 
            onEditClick = { transaction -> showEditTransactionDialog(transaction) },
            onDeleteClick = { transaction -> showDeleteConfirmationDialog(transaction) }
        )
        recyclerView.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.addTransactionFab).setOnClickListener {
            showAddTransactionDialog()
        }

        // Cargar transacciones existentes
        loadTransactions()
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
                    val transaction = Transaction(
                        description = description,
                        amount = amount,
                        type = if (isIncome) "Ingreso" else "Gasto",
                        category = category
                    )
                    saveTransaction(transaction)
                    adapter.addTransaction(transaction)
                    updateBalance()
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
        
        // Pre-llenar los campos con los datos existentes
        dialogView.findViewById<EditText>(R.id.descriptionInput).setText(transaction.description)
        dialogView.findViewById<EditText>(R.id.amountInput).setText(transaction.amount.toString())
        dialogView.findViewById<EditText>(R.id.categoryInput).setText(transaction.category)
        
        if (transaction.type == "Ingreso") {
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
                    val updatedTransaction = Transaction(
                        description = description,
                        amount = amount,
                        type = if (isIncome) "Ingreso" else "Gasto",
                        category = category,
                        date = transaction.date
                    )
                    updateTransaction(transaction, updatedTransaction)
                    loadTransactions() // Recargar la lista
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
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Transacción")
            .setMessage("¿Estás seguro de que deseas eliminar esta transacción?")
            .setPositiveButton("Eliminar") { dialog, _ ->
                deleteTransaction(transaction)
                loadTransactions() // Recargar la lista
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateTransaction(oldTransaction: Transaction, newTransaction: Transaction) {
        val db = dbHelper.writableDatabase
        
        val values = ContentValues().apply {
            put(FinanzasContract.TransaccionEntry.COLUMN_DESCRIPCION, newTransaction.description)
            put(FinanzasContract.TransaccionEntry.COLUMN_MONTO, newTransaction.amount)
            put(FinanzasContract.TransaccionEntry.COLUMN_TIPO, newTransaction.type)
            put(FinanzasContract.TransaccionEntry.COLUMN_CATEGORIA, newTransaction.category)
        }

        val selection = "${FinanzasContract.TransaccionEntry.COLUMN_DESCRIPCION} = ? AND " +
                       "${FinanzasContract.TransaccionEntry.COLUMN_FECHA} = ?"
        val selectionArgs = arrayOf(oldTransaction.description, oldTransaction.date.time.toString())

        db.update(
            FinanzasContract.TransaccionEntry.TABLE_NAME,
            values,
            selection,
            selectionArgs
        )
    }

    private fun deleteTransaction(transaction: Transaction) {
        val db = dbHelper.writableDatabase
        
        val selection = "${FinanzasContract.TransaccionEntry.COLUMN_DESCRIPCION} = ? AND " +
                       "${FinanzasContract.TransaccionEntry.COLUMN_FECHA} = ?"
        val selectionArgs = arrayOf(transaction.description, transaction.date.time.toString())

        db.delete(
            FinanzasContract.TransaccionEntry.TABLE_NAME,
            selection,
            selectionArgs
        )
    }

    private fun saveTransaction(transaction: Transaction) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(FinanzasContract.TransaccionEntry.COLUMN_DESCRIPCION, transaction.description)
            put(FinanzasContract.TransaccionEntry.COLUMN_MONTO, transaction.amount)
            put(FinanzasContract.TransaccionEntry.COLUMN_TIPO, transaction.type)
            put(FinanzasContract.TransaccionEntry.COLUMN_CATEGORIA, transaction.category)
            put(FinanzasContract.TransaccionEntry.COLUMN_FECHA, transaction.date.time)
        }

        db.insert(FinanzasContract.TransaccionEntry.TABLE_NAME, null, values)
    }

    private fun loadTransactions() {
        val db = dbHelper.readableDatabase
        val transactions = mutableListOf<Transaction>()

        val cursor = db.query(
            FinanzasContract.TransaccionEntry.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "${FinanzasContract.TransaccionEntry.COLUMN_FECHA} DESC"
        )

        with(cursor) {
            while (moveToNext()) {
                val description = getString(getColumnIndexOrThrow(FinanzasContract.TransaccionEntry.COLUMN_DESCRIPCION))
                val amount = getDouble(getColumnIndexOrThrow(FinanzasContract.TransaccionEntry.COLUMN_MONTO))
                val type = getString(getColumnIndexOrThrow(FinanzasContract.TransaccionEntry.COLUMN_TIPO))
                val category = getString(getColumnIndexOrThrow(FinanzasContract.TransaccionEntry.COLUMN_CATEGORIA))
                val date = Date(getLong(getColumnIndexOrThrow(FinanzasContract.TransaccionEntry.COLUMN_FECHA)))

                transactions.add(Transaction(description, amount, type, category, date))
            }
        }
        cursor.close()

        adapter = TransactionAdapter(transactions, 
            onEditClick = { transaction -> showEditTransactionDialog(transaction) },
            onDeleteClick = { transaction -> showDeleteConfirmationDialog(transaction) }
        )
        recyclerView.adapter = adapter
        updateBalance()
    }

    fun updateBalance() {
        val db = dbHelper.readableDatabase
        var balance = 0.0

        val cursor = db.query(
            FinanzasContract.TransaccionEntry.TABLE_NAME,
            arrayOf(FinanzasContract.TransaccionEntry.COLUMN_MONTO, FinanzasContract.TransaccionEntry.COLUMN_TIPO),
            null,
            null,
            null,
            null,
            null
        )

        with(cursor) {
            while (moveToNext()) {
                val amount = getDouble(getColumnIndexOrThrow(FinanzasContract.TransaccionEntry.COLUMN_MONTO))
                val type = getString(getColumnIndexOrThrow(FinanzasContract.TransaccionEntry.COLUMN_TIPO))
                
                if (type == "Ingreso") {
                    balance += amount
                } else {
                    balance -= amount
                }
            }
        }
        cursor.close()

        totalBalance = balance
        view?.findViewById<TextView>(R.id.balanceTotal)?.text = 
            CurrencyUtils.formatAmount(requireContext(), balance)
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}

class TransactionAdapter(
    private val transactions: MutableList<Transaction>,
    private val onEditClick: (Transaction) -> Unit,
    private val onDeleteClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val description: TextView = view.findViewById(R.id.descriptionText)
        val amount: TextView = view.findViewById(R.id.amountText)
        val date: TextView = view.findViewById(R.id.dateText)
        val category: TextView = view.findViewById(R.id.categoryText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        val context = holder.itemView.context
        
        holder.description.text = transaction.description
        holder.amount.text = CurrencyUtils.formatAmount(context, transaction.amount)
        holder.date.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .format(transaction.date)
        holder.category.text = transaction.category
        
        holder.amount.setTextColor(
            holder.amount.context.getColor(
                if (transaction.type == "Ingreso") R.color.income_green else R.color.expense_red
            )
        )

        // Configurar el menú contextual al mantener presionado
        holder.itemView.setOnLongClickListener { view ->
            showPopupMenu(view, transaction)
            true
        }
    }

    private fun showPopupMenu(view: View, transaction: Transaction) {
        val popup = PopupMenu(view.context, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.transaccion_context_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_editar -> {
                    onEditClick(transaction)
                    true
                }
                R.id.menu_eliminar -> {
                    onDeleteClick(transaction)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun getItemCount() = transactions.size

    fun addTransaction(transaction: Transaction) {
        transactions.add(0, transaction)
        notifyItemInserted(0)
    }

    fun refreshAmounts() {
        notifyDataSetChanged()
    }
} 