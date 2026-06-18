package com.example.ahorrapp.ui.wallets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ahorrapp.R
import com.example.ahorrapp.adapter.WalletAdapter
import com.example.ahorrapp.data.model.Wallet
import com.example.ahorrapp.data.repository.WalletRepository
import com.example.ahorrapp.databinding.DialogAddWalletBinding
import com.example.ahorrapp.databinding.FragmentWalletsBinding
import com.example.ahorrapp.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WalletsFragment : Fragment() {

    @Inject
    lateinit var walletRepository: WalletRepository

    private lateinit var sessionManager: SessionManager
    private var _binding: FragmentWalletsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: WalletAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalletsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sessionManager = SessionManager(requireContext())
        
        setupRecyclerView()
        setupFab()
        loadWallets()
    }

    private fun setupRecyclerView() {
        adapter = WalletAdapter(
            onEditClick = { wallet -> showEditWalletDialog(wallet) },
            onDeleteClick = { wallet -> showDeleteConfirmationDialog(wallet) },
            onToggleActive = { wallet, isActive -> toggleWalletActive(wallet, isActive) }
        )
        binding.walletsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.walletsRecyclerView.adapter = adapter
    }

    private fun setupFab() {
        binding.addWalletFab.setOnClickListener {
            showAddWalletDialog()
        }
    }

    private fun loadWallets() {
        viewLifecycleOwner.lifecycleScope.launch {
            walletRepository.getWalletsByUser(sessionManager.getUserId()).collect { wallets ->
                adapter.submitList(wallets)
            }
        }
    }

    private fun showAddWalletDialog() {
        val dialogBinding = DialogAddWalletBinding.inflate(layoutInflater)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_wallet)
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                val name = dialogBinding.walletNameInput.text.toString().trim()
                if (name.isNotEmpty()) {
                    addWallet(name)
                } else {
                    Toast.makeText(requireContext(), "Por favor ingresa un nombre", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditWalletDialog(wallet: Wallet) {
        val dialogBinding = DialogAddWalletBinding.inflate(layoutInflater)
        dialogBinding.walletNameInput.setText(wallet.name)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.edit_wallet)
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                val name = dialogBinding.walletNameInput.text.toString().trim()
                if (name.isNotEmpty()) {
                    updateWallet(wallet.copy(name = name))
                } else {
                    Toast.makeText(requireContext(), "Por favor ingresa un nombre", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(wallet: Wallet) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_wallet)
            .setMessage(R.string.delete_wallet_confirmation)
            .setPositiveButton("Eliminar") { _, _ ->
                deleteWallet(wallet)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun addWallet(name: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val wallet = Wallet(
                userId = sessionManager.getUserId(),
                name = name,
                isActive = true
            )
            val result = walletRepository.addWallet(wallet)
            if (result.isSuccess) {
                Toast.makeText(requireContext(), "Billetera agregada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Error al agregar billetera", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateWallet(wallet: Wallet) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = walletRepository.updateWallet(wallet)
            if (result.isSuccess) {
                Toast.makeText(requireContext(), "Billetera actualizada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Error al actualizar billetera", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteWallet(wallet: Wallet) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = walletRepository.deleteWallet(wallet)
            if (result.isSuccess) {
                Toast.makeText(requireContext(), "Billetera eliminada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Error al eliminar billetera", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleWalletActive(wallet: Wallet, isActive: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            val result = walletRepository.updateWallet(wallet.copy(isActive = isActive))
            if (result.isSuccess) {
                Toast.makeText(
                    requireContext(),
                    if (isActive) "Billetera activada" else "Billetera desactivada",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(requireContext(), "Error al actualizar billetera", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}





