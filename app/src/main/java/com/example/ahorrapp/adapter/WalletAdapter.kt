package com.example.ahorrapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ahorrapp.R
import com.example.ahorrapp.data.model.Wallet
import com.example.ahorrapp.databinding.ItemWalletBinding

class WalletAdapter(
    private val onEditClick: (Wallet) -> Unit,
    private val onDeleteClick: (Wallet) -> Unit,
    private val onToggleActive: (Wallet, Boolean) -> Unit
) : ListAdapter<Wallet, WalletAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWalletBinding.inflate(
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
        private val binding: ItemWalletBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(wallet: Wallet) {
            binding.walletNameText.text = wallet.name
            binding.walletStatusText.text = if (wallet.isActive) {
                binding.root.context.getString(R.string.active)
            } else {
                binding.root.context.getString(R.string.inactive)
            }
            binding.walletActiveSwitch.isChecked = wallet.isActive

            binding.walletActiveSwitch.setOnCheckedChangeListener { _, isChecked ->
                onToggleActive(wallet, isChecked)
            }

            binding.editWalletButton.setOnClickListener {
                onEditClick(wallet)
            }

            binding.deleteWalletButton.setOnClickListener {
                onDeleteClick(wallet)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Wallet>() {
        override fun areItemsTheSame(oldItem: Wallet, newItem: Wallet): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Wallet, newItem: Wallet): Boolean {
            return oldItem == newItem
        }
    }
}

