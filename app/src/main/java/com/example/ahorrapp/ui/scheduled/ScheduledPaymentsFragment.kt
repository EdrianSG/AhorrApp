package com.example.ahorrapp.ui.scheduled

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ahorrapp.adapter.ScheduledPaymentAdapter
import com.example.ahorrapp.data.model.RepeatInterval
import com.example.ahorrapp.data.model.ScheduledPayment
import com.example.ahorrapp.databinding.DialogAddScheduledPaymentBinding
import com.example.ahorrapp.databinding.FragmentScheduledPaymentsBinding
import com.example.ahorrapp.utils.SessionManager
import com.example.ahorrapp.viewmodel.ScheduledPaymentViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ScheduledPaymentsFragment : Fragment() {
    private var _binding: FragmentScheduledPaymentsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ScheduledPaymentViewModel by viewModels()
    private lateinit var adapter: ScheduledPaymentAdapter

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduledPaymentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFilterChips()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ScheduledPaymentAdapter(
            onEditClick = { payment ->
                showEditPaymentDialog(payment)
            },
            onDeleteClick = { payment ->
                showDeleteConfirmationDialog(payment)
            }
        )
        binding.scheduledPaymentsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ScheduledPaymentsFragment.adapter
        }
    }

    private fun setupFilterChips() {
        binding.filterChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val chip = group.findViewById<Chip>(checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener)
            when (chip.id) {
                binding.allChip.id -> showAllPayments()
                binding.activeChip.id -> showActivePayments()
                binding.inactiveChip.id -> showInactivePayments()
            }
        }
    }

    private fun setupFab() {
        binding.addScheduledPaymentFab.setOnClickListener {
            showAddPaymentDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.scheduledPayments.observe(viewLifecycleOwner) { payments ->
            adapter.submitList(payments)
            binding.emptyView.visibility = if (payments.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.paymentResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = {
                    Toast.makeText(requireContext(), "Operación exitosa", Toast.LENGTH_SHORT).show()
                },
                onFailure = { exception ->
                    Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun showAllPayments() {
        viewModel.loadAllPayments()
    }

    private fun showActivePayments() {
        viewModel.loadActivePayments()
    }

    private fun showInactivePayments() {
        viewModel.loadInactivePayments()
    }

    private fun showAddPaymentDialog() {
        val dialogBinding = DialogAddScheduledPaymentBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Programar pago")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                saveScheduledPayment(dialogBinding)
            }
            .setNegativeButton("Cancelar", null)
            .create()

        // Configurar el spinner de intervalos de repetición
        val repeatIntervals = RepeatInterval.values().map { it.displayName }
        (dialogBinding.repeatIntervalInput as? AutoCompleteTextView)?.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, repeatIntervals)
        )

        // Configurar las categorías predefinidas
        val categories = listOf(
            "Alquiler/Hipoteca",
            "Servicios básicos",
            "Internet/Telefonía",
            "Suscripciones",
            "Seguros",
            "Préstamos",
            "Educación",
            "Gimnasio",
            "Mantenimiento",
            "Otros"
        )
        (dialogBinding.categoryInput as? AutoCompleteTextView)?.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        )

        // Configurar el checkbox de fecha fin
        dialogBinding.hasEndDateCheckbox.setOnCheckedChangeListener { _, isChecked ->
            dialogBinding.endDatePicker.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Mostrar texto informativo sobre la hora de notificación
        dialogBinding.notificationInfoText.text = "Las notificaciones se enviarán a las 9:00 AM en las fechas programadas"
        dialogBinding.notificationInfoText.visibility = View.VISIBLE

        dialog.show()
    }

    private fun showEditPaymentDialog(payment: ScheduledPayment) {
        val dialogBinding = DialogAddScheduledPaymentBinding.inflate(layoutInflater)
        
        // Pre-llenar los campos con los datos existentes
        with(dialogBinding) {
            titleInput.setText(payment.title)
            descriptionInput.setText(payment.description)
            amountInput.setText(payment.amount.toString())
            categoryInput.setText(payment.category)
            repeatIntervalInput.setText(payment.repeatInterval.displayName)
            
            // Configurar fecha de inicio
            val startCalendar = Calendar.getInstance().apply { time = payment.startDate }
            startDatePicker.updateDate(
                startCalendar.get(Calendar.YEAR),
                startCalendar.get(Calendar.MONTH),
                startCalendar.get(Calendar.DAY_OF_MONTH)
            )
            
            // Configurar fecha de fin si existe
            payment.endDate?.let { endDate ->
                hasEndDateCheckbox.isChecked = true
                endDatePicker.visibility = View.VISIBLE
                val endCalendar = Calendar.getInstance().apply { time = endDate }
                endDatePicker.updateDate(
                    endCalendar.get(Calendar.YEAR),
                    endCalendar.get(Calendar.MONTH),
                    endCalendar.get(Calendar.DAY_OF_MONTH)
                )
            }

            // Mostrar texto informativo sobre la hora de notificación
            notificationInfoText.text = "Las notificaciones se enviarán a las 9:00 AM en las fechas programadas"
            notificationInfoText.visibility = View.VISIBLE
        }

        // Configurar el spinner de intervalos de repetición
        val repeatIntervals = RepeatInterval.values().map { it.displayName }
        (dialogBinding.repeatIntervalInput as? AutoCompleteTextView)?.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, repeatIntervals)
        )

        // Configurar las categorías predefinidas
        val categories = listOf(
            "Alquiler/Hipoteca",
            "Servicios básicos",
            "Internet/Telefonía",
            "Suscripciones",
            "Seguros",
            "Préstamos",
            "Educación",
            "Gimnasio",
            "Mantenimiento",
            "Otros"
        )
        (dialogBinding.categoryInput as? AutoCompleteTextView)?.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        )

        // Configurar el checkbox de fecha fin
        dialogBinding.hasEndDateCheckbox.setOnCheckedChangeListener { _, isChecked ->
            dialogBinding.endDatePicker.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Editar pago programado")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                updateScheduledPayment(dialogBinding, payment)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveScheduledPayment(dialogBinding: DialogAddScheduledPaymentBinding) {
        try {
            val title = dialogBinding.titleInput.text.toString()
            val description = dialogBinding.descriptionInput.text.toString()
            val amount = dialogBinding.amountInput.text.toString().toDoubleOrNull() ?: 0.0
            
            val startCalendar = Calendar.getInstance()
            with(dialogBinding.startDatePicker) {
                startCalendar.set(year, month, dayOfMonth)
            }

            var endDate: Date? = null
            if (dialogBinding.hasEndDateCheckbox.isChecked) {
                val endCalendar = Calendar.getInstance()
                with(dialogBinding.endDatePicker) {
                    endCalendar.set(year, month, dayOfMonth)
                }
                endDate = endCalendar.time
            }

            val repeatIntervalName = dialogBinding.repeatIntervalInput.text.toString()
            val repeatInterval = RepeatInterval.values().first { it.displayName == repeatIntervalName }

            val category = dialogBinding.categoryInput.text.toString()

            viewModel.addScheduledPayment(
                title = title,
                description = description,
                amount = amount,
                startDate = startCalendar.time,
                endDate = endDate,
                repeatInterval = repeatInterval,
                category = category
            )
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al guardar el pago programado: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateScheduledPayment(dialogBinding: DialogAddScheduledPaymentBinding, originalPayment: ScheduledPayment) {
        try {
            val title = dialogBinding.titleInput.text.toString()
            val description = dialogBinding.descriptionInput.text.toString()
            val amount = dialogBinding.amountInput.text.toString().toDoubleOrNull() ?: 0.0
            
            val startCalendar = Calendar.getInstance()
            with(dialogBinding.startDatePicker) {
                startCalendar.set(year, month, dayOfMonth)
            }

            var endDate: Date? = null
            if (dialogBinding.hasEndDateCheckbox.isChecked) {
                val endCalendar = Calendar.getInstance()
                with(dialogBinding.endDatePicker) {
                    endCalendar.set(year, month, dayOfMonth)
                }
                endDate = endCalendar.time
            }

            val repeatIntervalName = dialogBinding.repeatIntervalInput.text.toString()
            val repeatInterval = RepeatInterval.values().first { it.displayName == repeatIntervalName }

            val category = dialogBinding.categoryInput.text.toString()

            val updatedPayment = originalPayment.copy(
                title = title,
                description = description,
                amount = amount,
                startDate = startCalendar.time,
                endDate = endDate,
                repeatInterval = repeatInterval,
                category = category
            )

            viewModel.updateScheduledPayment(updatedPayment)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al actualizar el pago programado: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showDeleteConfirmationDialog(payment: ScheduledPayment) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar pago programado")
            .setMessage("¿Estás seguro de que deseas eliminar el pago programado '${payment.title}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deletePayment(payment)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 