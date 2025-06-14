package com.example.ahorrapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.ahorrapp.R
import com.example.ahorrapp.data.model.TransactionCategory

class CategoryAdapter(
    private val categories: List<TransactionCategory>,
    private val onCategorySelected: (TransactionCategory) -> Unit
) : BaseAdapter() {

    private var selectedPosition: Int = -1

    override fun getCount(): Int = categories.size

    override fun getItem(position: Int): TransactionCategory = categories[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)

        val category = getItem(position)
        view.findViewById<ImageView>(R.id.categoryIcon).setImageResource(category.iconResourceId)
        view.findViewById<TextView>(R.id.categoryName).text = category.name

        // Aplicar el estado de selección
        if (position == selectedPosition) {
            view.setBackgroundColor(ContextCompat.getColor(parent.context, R.color.selected_category_background))
        } else {
            view.setBackgroundColor(ContextCompat.getColor(parent.context, android.R.color.transparent))
        }

        view.setOnClickListener { 
            selectedPosition = position
            notifyDataSetChanged()
            onCategorySelected(category)
        }

        return view
    }
} 