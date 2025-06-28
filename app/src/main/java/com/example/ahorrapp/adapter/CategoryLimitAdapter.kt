package com.example.ahorrapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ahorrapp.R
import com.example.ahorrapp.data.model.CategoryLimit
import com.example.ahorrapp.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*

class CategoryLimitAdapter(
    private val onEditClick: (CategoryLimit) -> Unit,
    private val onDeleteClick: (CategoryLimit) -> Unit
) : ListAdapter<CategoryLimit, CategoryLimitAdapter.CategoryLimitViewHolder>(CategoryLimitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryLimitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_limit, parent, false)
        return CategoryLimitViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryLimitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryLimitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryText: TextView = itemView.findViewById(R.id.categoryText)
        private val periodText: TextView = itemView.findViewById(R.id.periodText)
        private val spentAmountText: TextView = itemView.findViewById(R.id.spentAmountText)
        private val limitAmountText: TextView = itemView.findViewById(R.id.limitAmountText)
        private val progressBar: com.google.android.material.progressindicator.LinearProgressIndicator = 
            itemView.findViewById(R.id.progressBar)
        private val progressText: TextView = itemView.findViewById(R.id.progressText)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(limit: CategoryLimit) {
            val context = itemView.context
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            categoryText.text = limit.category
            periodText.text = "${dateFormat.format(limit.startDate)} - ${dateFormat.format(limit.endDate)}"
            
            spentAmountText.text = CurrencyUtils.formatAmount(context, limit.currentSpent)
            limitAmountText.text = "/ ${CurrencyUtils.formatAmount(context, limit.limitAmount)}"
            
            val progress = limit.progress.toInt()
            progressBar.progress = progress
            
            val remaining = limit.remainingAmount
            progressText.text = "${progress}% gastado - ${CurrencyUtils.formatAmount(context, remaining)} restante"
            
            // Cambiar color de la barra según el progreso
            when {
                limit.isOverLimit -> {
                    progressBar.setIndicatorColor(context.getColor(android.R.color.holo_red_dark))
                    progressText.text = "¡Límite excedido!"
                }
                limit.isNearLimit -> {
                    progressBar.setIndicatorColor(context.getColor(android.R.color.holo_orange_dark))
                }
                progress >= 75 -> {
                    progressBar.setIndicatorColor(context.getColor(android.R.color.holo_orange_light))
                }
                else -> {
                    progressBar.setIndicatorColor(context.getColor(android.R.color.holo_green_light))
                }
            }

            editButton.setOnClickListener { onEditClick(limit) }
            deleteButton.setOnClickListener { onDeleteClick(limit) }
        }
    }

    private class CategoryLimitDiffCallback : DiffUtil.ItemCallback<CategoryLimit>() {
        override fun areItemsTheSame(oldItem: CategoryLimit, newItem: CategoryLimit): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CategoryLimit, newItem: CategoryLimit): Boolean {
            return oldItem == newItem
        }
    }
} 