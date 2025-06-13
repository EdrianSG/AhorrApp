package com.example.ahorrapp.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ahorrapp.databinding.FragmentCalendarBinding
import com.example.ahorrapp.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CalendarViewModel by viewModels()
    private lateinit var transactionsAdapter: TransactionsAdapter
    
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            setupRecyclerView()
            setupCalendarView()
            observeTransactions()
            
            // Cargar las transacciones del día actual
            if (sessionManager.isLoggedIn()) {
                val calendar = Calendar.getInstance()
                viewModel.loadTransactionsForDate(sessionManager.getUserId(), calendar.timeInMillis)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al inicializar el calendario: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerView() {
        transactionsAdapter = TransactionsAdapter()
        binding.transactionsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionsAdapter
        }
    }

    private fun setupCalendarView() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            try {
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth)
                
                if (sessionManager.isLoggedIn()) {
                    val userId = sessionManager.getUserId()
                    viewModel.loadTransactionsForDate(userId, calendar.timeInMillis)
                } else {
                    Toast.makeText(requireContext(), "Por favor, inicia sesión", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar las transacciones: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun observeTransactions() {
        viewModel.transactions.observe(viewLifecycleOwner) { transactions ->
            try {
                transactionsAdapter.updateTransactions(transactions ?: emptyList())
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al actualizar las transacciones: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 