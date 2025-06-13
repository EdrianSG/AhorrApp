package com.example.ahorrapp.ui.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ahorrapp.data.model.Transaction
import com.example.ahorrapp.databinding.ItemTransactionCalendarBinding
import com.example.ahorrapp.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*

class TransactionsAdapter : RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

    private var transactions: List<Transaction> = emptyList()
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionCalendarBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionCalendarBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            with(binding) {
                tvTime.text = dateFormat.format(transaction.date)
                tvDescription.text = transaction.description
                tvAmount.text = CurrencyUtils.formatAmount(itemView.context, transaction.amount)
                
                // Establecer color según el tipo de transacción
                tvAmount.setTextColor(
                    itemView.context.getColor(
                        if (transaction.type == "INGRESO") android.R.color.holo_green_dark
                        else android.R.color.holo_red_dark
                    )
                )
            }
        }
    }
} 