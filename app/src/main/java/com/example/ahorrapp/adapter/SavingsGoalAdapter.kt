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
import com.example.ahorrapp.data.model.SavingsGoal
import com.example.ahorrapp.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*

class SavingsGoalAdapter(
    private val onAddMoneyClick: (SavingsGoal) -> Unit,
    private val onEditClick: (SavingsGoal) -> Unit,
    private val onDeleteClick: (SavingsGoal) -> Unit
) : ListAdapter<SavingsGoal, SavingsGoalAdapter.SavingsGoalViewHolder>(SavingsGoalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavingsGoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_savings_goal, parent, false)
        return SavingsGoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavingsGoalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SavingsGoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.nameText)
        private val targetDateText: TextView = itemView.findViewById(R.id.targetDateText)
        private val currentAmountText: TextView = itemView.findViewById(R.id.currentAmountText)
        private val targetAmountText: TextView = itemView.findViewById(R.id.targetAmountText)
        private val progressBar: com.google.android.material.progressindicator.LinearProgressIndicator = 
            itemView.findViewById(R.id.progressBar)
        private val progressText: TextView = itemView.findViewById(R.id.progressText)
        private val addMoneyButton: ImageButton = itemView.findViewById(R.id.addMoneyButton)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(goal: SavingsGoal) {
            val context = itemView.context
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            nameText.text = goal.name
            targetDateText.text = "Objetivo: ${dateFormat.format(goal.targetDate)}"
            
            currentAmountText.text = CurrencyUtils.formatAmount(context, goal.currentAmount)
            targetAmountText.text = "/ ${CurrencyUtils.formatAmount(context, goal.targetAmount)}"
            
            val progress = goal.progress.toInt()
            progressBar.progress = progress
            
            progressText.text = "${progress}% completado"
            
            // Cambiar color de la barra según el progreso
            when {
                progress >= 100 -> {
                    progressBar.setIndicatorColor(context.getColor(android.R.color.holo_green_dark))
                    progressText.text = "¡Meta completada!"
                }
                progress >= 75 -> {
                    progressBar.setIndicatorColor(context.getColor(android.R.color.holo_green_light))
                }
                progress >= 50 -> {
                    progressBar.setIndicatorColor(context.getColor(android.R.color.holo_orange_light))
                }
                else -> {
                    progressBar.setIndicatorColor(context.getColor(android.R.color.holo_red_light))
                }
            }

            addMoneyButton.setOnClickListener { onAddMoneyClick(goal) }
            editButton.setOnClickListener { onEditClick(goal) }
            deleteButton.setOnClickListener { onDeleteClick(goal) }
        }
    }

    private class SavingsGoalDiffCallback : DiffUtil.ItemCallback<SavingsGoal>() {
        override fun areItemsTheSame(oldItem: SavingsGoal, newItem: SavingsGoal): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SavingsGoal, newItem: SavingsGoal): Boolean {
            return oldItem == newItem
        }
    }
} 