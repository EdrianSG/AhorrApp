package com.example.ahorrapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ahorrapp.data.model.Transaction
import com.example.ahorrapp.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val onEditClick: (Transaction) -> Unit,
    private val onDeleteClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.bind(transaction)
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val description: TextView = itemView.findViewById(R.id.transactionDescription)
        private val amount: TextView = itemView.findViewById(R.id.transactionAmount)
        private val category: TextView = itemView.findViewById(R.id.transactionCategory)
        private val date: TextView = itemView.findViewById(R.id.transactionDate)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(transaction: Transaction) {
            description.text = transaction.description
            amount.text = CurrencyUtils.formatAmount(itemView.context, transaction.amount)
            category.text = transaction.category
            date.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(transaction.date)

            amount.setTextColor(
                amount.context.getColor(
                    if (transaction.type == "INGRESO") R.color.income_green else R.color.expense_red
                )
            )

            editButton.setOnClickListener { onEditClick(transaction) }
            deleteButton.setOnClickListener { onDeleteClick(transaction) }
        }
    }
}

private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
    override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem == newItem
    }
} 