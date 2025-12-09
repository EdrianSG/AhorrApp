package com.example.ahorrapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.ahorrapp.R
import com.example.ahorrapp.data.model.Wallet
import com.example.ahorrapp.utils.CurrencyUtils

data class WalletWithBalance(
    val wallet: Wallet,
    val balance: Double
) {
    private var displayText: String? = null
    
    fun getDisplayText(context: Context?): String {
        if (displayText == null) {
            displayText = "${wallet.name} - ${CurrencyUtils.formatAmount(context, balance)}"
        }
        return displayText ?: "${wallet.name} - ${CurrencyUtils.formatAmount(context, balance)}"
    }
    
    override fun toString(): String {
        // Usar el texto de display si está disponible, sino usar el nombre de la billetera
        return displayText ?: wallet.name
    }
}

class WalletWithBalanceAdapter(
    context: Context,
    private val walletsWithBalance: List<WalletWithBalance>
) : ArrayAdapter<WalletWithBalance>(context, android.R.layout.simple_spinner_item, walletsWithBalance) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_spinner_item, parent, false)
        
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = walletsWithBalance[position].getDisplayText(context)
        
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false)
        
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = walletsWithBalance[position].getDisplayText(context)
        
        return view
    }
}

