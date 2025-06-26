package com.example.ahorrapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ahorrapp.data.model.RepeatInterval
import com.example.ahorrapp.data.model.ScheduledPayment
import com.example.ahorrapp.databinding.ItemScheduledPaymentBinding
import com.example.ahorrapp.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*

class ScheduledPaymentAdapter(
    private val onEditClick: (ScheduledPayment) -> Unit,
    private val onDeleteClick: (ScheduledPayment) -> Unit
) : ListAdapter<ScheduledPayment, ScheduledPaymentAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScheduledPaymentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemScheduledPaymentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.editButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEditClick(getItem(position))
                }
            }

            binding.deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position))
                }
            }
        }

        fun bind(payment: ScheduledPayment) {
            with(binding) {
                titleText.text = payment.title
                descriptionText.text = payment.description
                amountText.text = CurrencyUtils.formatAmount(itemView.context, payment.amount)
                amountText.setTextColor(
                    itemView.context.getColor(android.R.color.holo_red_dark)
                )

                repeatIntervalChip.text = getRepeatIntervalText(payment.repeatInterval)
                nextPaymentChip.text = "Próximo: ${formatDate(payment.startDate)}"
                statusChip.text = if (payment.isActive) "Activo" else "Inactivo"
                statusChip.setChipBackgroundColorResource(
                    if (payment.isActive) android.R.color.holo_green_light
                    else android.R.color.darker_gray
                )
            }
        }

        private fun getRepeatIntervalText(interval: RepeatInterval): String {
            return when (interval) {
                RepeatInterval.NONE -> "Una vez"
                RepeatInterval.DAILY -> "Diario"
                RepeatInterval.WEEKLY -> "Semanal"
                RepeatInterval.MONTHLY -> "Mensual"
                RepeatInterval.YEARLY -> "Anual"
            }
        }

        private fun formatDate(date: Date): String {
            return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ScheduledPayment>() {
        override fun areItemsTheSame(oldItem: ScheduledPayment, newItem: ScheduledPayment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ScheduledPayment, newItem: ScheduledPayment): Boolean {
            return oldItem == newItem
        }
    }
} 