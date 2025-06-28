package com.example.ahorrapp.ui.scheduled

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ahorrapp.adapter.CategoryLimitAdapter
import com.example.ahorrapp.adapter.SavingsGoalAdapter
import com.example.ahorrapp.adapter.ScheduledPaymentAdapter
import com.example.ahorrapp.data.model.CategoryLimit
import com.example.ahorrapp.data.model.RepeatInterval
import com.example.ahorrapp.data.model.SavingsGoal
import com.example.ahorrapp.data.model.ScheduledPayment
import com.example.ahorrapp.data.model.Categories
import com.example.ahorrapp.databinding.DialogAddCategoryLimitBinding
import com.example.ahorrapp.databinding.DialogAddMoneyToGoalBinding
import com.example.ahorrapp.databinding.DialogAddSavingsGoalBinding
import com.example.ahorrapp.databinding.DialogAddScheduledPaymentBinding
import com.example.ahorrapp.databinding.FragmentScheduledPaymentsBinding
import com.example.ahorrapp.utils.CurrencyUtils
import com.example.ahorrapp.utils.SessionManager
import com.example.ahorrapp.viewmodel.CategoryLimitViewModel
import com.example.ahorrapp.viewmodel.SavingsGoalViewModel
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
    
    private val scheduledPaymentViewModel: ScheduledPaymentViewModel by viewModels()
    private val savingsGoalViewModel: SavingsGoalViewModel by viewModels()
    private val categoryLimitViewModel: CategoryLimitViewModel by viewModels()
    
    private lateinit var scheduledPaymentAdapter: ScheduledPaymentAdapter
    private lateinit var savingsGoalAdapter: SavingsGoalAdapter
    private lateinit var categoryLimitAdapter: CategoryLimitAdapter

    @Inject
    lateinit var sessionManager: SessionManager

    companion object {
        private const val TAG = "ScheduledPaymentsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: Iniciando creación de vista")
        _binding = FragmentScheduledPaymentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Configurando componentes")
        try {
            // Verificar el userId actual
            val currentUserId = sessionManager.getUserId()
            Log.d(TAG, "onViewCreated: Usuario actual - userId: $currentUserId, isLoggedIn: ${sessionManager.isLoggedIn()}")
            
            setupRecyclerViews()
            setupFilterChips()
            setupFab()
            observeViewModels()
        } catch (e: Exception) {
            Log.e(TAG, "onViewCreated: Error al configurar componentes", e)
            Toast.makeText(requireContext(), "Error al cargar la pantalla: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerViews() {
        Log.d(TAG, "setupRecyclerViews: Configurando RecyclerViews")
        
        try {
            // Setup Scheduled Payments RecyclerView
            scheduledPaymentAdapter = ScheduledPaymentAdapter(
                onEditClick = { payment -> showEditPaymentDialog(payment) },
                onDeleteClick = { payment -> showDeletePaymentConfirmationDialog(payment) }
            )
            binding.scheduledPaymentsList.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = scheduledPaymentAdapter
            }

            // Setup Savings Goals RecyclerView
            savingsGoalAdapter = SavingsGoalAdapter(
                onAddMoneyClick = { goal -> showAddMoneyToGoalDialog(goal) },
                onEditClick = { goal -> showEditSavingsGoalDialog(goal) },
                onDeleteClick = { goal -> showDeleteSavingsGoalConfirmationDialog(goal) }
            )
            binding.savingsGoalsList.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = savingsGoalAdapter
            }

            // Setup Category Limits RecyclerView
            Log.d(TAG, "setupRecyclerViews: Configurando CategoryLimitAdapter")
            categoryLimitAdapter = CategoryLimitAdapter(
                onEditClick = { limit -> 
                    Log.d(TAG, "CategoryLimitAdapter onEditClick: ${limit.category}")
                    showEditCategoryLimitDialog(limit) 
                },
                onDeleteClick = { limit -> 
                    Log.d(TAG, "CategoryLimitAdapter onDeleteClick: ${limit.category}")
                    showDeleteCategoryLimitConfirmationDialog(limit) 
                }
            )
            binding.categoryLimitsList.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = categoryLimitAdapter
            }
            Log.d(TAG, "setupRecyclerViews: RecyclerViews configurados correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "setupRecyclerViews: Error al configurar RecyclerViews", e)
            Toast.makeText(requireContext(), "Error al configurar listas: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFilterChips() {
        Log.d(TAG, "setupFilterChips: Configurando filtros")
        
        // Setup Payments Filter Chips - Solo mostrar todos los pagos
        binding.paymentsFilterChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            // Siempre cargar todos los pagos
            scheduledPaymentViewModel.loadAllPayments()
        }

        // Setup Limits Filter Chips
        binding.limitsFilterChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val chip = group.findViewById<Chip>(checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener)
            when (chip.id) {
                binding.limitsAllChip.id -> {
                    Log.d(TAG, "setupFilterChips: Cargando todos los límites")
                    categoryLimitViewModel.loadAllLimits()
                }
                binding.limitsActiveChip.id -> {
                    Log.d(TAG, "setupFilterChips: Cargando límites activos")
                    categoryLimitViewModel.loadActiveLimits()
                }
                binding.limitsNearLimitChip.id -> {
                    Log.d(TAG, "setupFilterChips: Cargando límites cerca del límite")
                    categoryLimitViewModel.loadNearLimitLimits()
                }
            }
        }
    }

    private fun setupFab() {
        binding.addScheduledPaymentFab.setOnClickListener {
            Log.d(TAG, "setupFab: FAB clickeado")
            showFabMenu()
        }
    }

    private fun showFabMenu() {
        Log.d(TAG, "showFabMenu: Mostrando menú FAB")
        val options = arrayOf("Pago Programado", "Meta de Ahorro", "Límite de Categoría")
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Agregar")
            .setItems(options) { _, which ->
                Log.d(TAG, "showFabMenu: Opción seleccionada: $which")
                when (which) {
                    0 -> showAddPaymentDialog()
                    1 -> showAddSavingsGoalDialog()
                    2 -> {
                        Log.d(TAG, "showFabMenu: Intentando mostrar diálogo de límite de categoría")
                        showAddCategoryLimitDialog()
                    }
                }
            }
            .show()
    }

    private fun observeViewModels() {
        Log.d(TAG, "observeViewModels: Configurando observadores")
        
        try {
            // Observe Scheduled Payments
            scheduledPaymentViewModel.scheduledPayments.observe(viewLifecycleOwner) { payments ->
                Log.d(TAG, "observeViewModels: Pagos programados recibidos: ${payments.size}")
                payments.forEach { payment ->
                    Log.d(TAG, "observeViewModels: Pago - ID: ${payment.id}, userId: ${payment.userId}, título: ${payment.title}")
                }
                scheduledPaymentAdapter.submitList(payments)
                binding.paymentsEmptyView.visibility = if (payments.isEmpty()) View.VISIBLE else View.GONE
            }

            scheduledPaymentViewModel.paymentResult.observe(viewLifecycleOwner) { result ->
                result.fold(
                    onSuccess = {
                        Toast.makeText(requireContext(), "Operación exitosa", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { exception ->
                        Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Observe Savings Goals
            savingsGoalViewModel.savingsGoals.observe(viewLifecycleOwner) { goals ->
                savingsGoalAdapter.submitList(goals)
                binding.savingsEmptyView.visibility = if (goals.isEmpty()) View.VISIBLE else View.GONE
            }

            savingsGoalViewModel.goalResult.observe(viewLifecycleOwner) { result ->
                result.fold(
                    onSuccess = {
                        Toast.makeText(requireContext(), "Meta guardada exitosamente", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { exception ->
                        Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            savingsGoalViewModel.addMoneyResult.observe(viewLifecycleOwner) { result ->
                result.fold(
                    onSuccess = {
                        Toast.makeText(requireContext(), "Dinero agregado exitosamente", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { exception ->
                        Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Observe Category Limits
            Log.d(TAG, "observeViewModels: Configurando observador de límites de categoría")
            categoryLimitViewModel.categoryLimits.observe(viewLifecycleOwner) { limits ->
                Log.d(TAG, "observeViewModels: Límites de categoría recibidos: ${limits.size}")
                try {
                    categoryLimitAdapter.submitList(limits)
                    binding.limitsEmptyView.visibility = if (limits.isEmpty()) View.VISIBLE else View.GONE
                } catch (e: Exception) {
                    Log.e(TAG, "observeViewModels: Error al actualizar lista de límites", e)
                }
            }

            categoryLimitViewModel.limitResult.observe(viewLifecycleOwner) { result ->
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "observeViewModels: Límite guardado exitosamente")
                        Toast.makeText(requireContext(), "Límite guardado exitosamente", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "observeViewModels: Error al guardar límite", exception)
                        Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "observeViewModels: Error al configurar observadores", e)
            Toast.makeText(requireContext(), "Error al configurar observadores: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Scheduled Payments Dialogs
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

        setupPaymentDialog(dialogBinding)
        dialog.show()
    }

    private fun setupPaymentDialog(dialogBinding: DialogAddScheduledPaymentBinding) {
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
    }

    private fun saveScheduledPayment(dialogBinding: DialogAddScheduledPaymentBinding) {
        val title = dialogBinding.titleInput.text.toString()
        val description = dialogBinding.descriptionInput.text.toString()
        val amountText = dialogBinding.amountInput.text.toString()
        val category = dialogBinding.categoryInput.text.toString()
        val repeatIntervalText = dialogBinding.repeatIntervalInput.text.toString()

        if (title.isEmpty() || amountText.isEmpty() || category.isEmpty() || repeatIntervalText.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(requireContext(), "Por favor ingresa un monto válido", Toast.LENGTH_SHORT).show()
            return
        }

        val repeatInterval = RepeatInterval.values().find { it.displayName == repeatIntervalText }
        if (repeatInterval == null) {
            Toast.makeText(requireContext(), "Por favor selecciona un intervalo válido", Toast.LENGTH_SHORT).show()
            return
        }

        val calendar = Calendar.getInstance()
        calendar.set(
            dialogBinding.startDatePicker.year,
            dialogBinding.startDatePicker.month,
            dialogBinding.startDatePicker.dayOfMonth
        )
        val startDate = calendar.time

        val endDate = if (dialogBinding.hasEndDateCheckbox.isChecked) {
            calendar.set(
                dialogBinding.endDatePicker.year,
                dialogBinding.endDatePicker.month,
                dialogBinding.endDatePicker.dayOfMonth
            )
            calendar.time
        } else null

        scheduledPaymentViewModel.addScheduledPayment(
            title = title,
            description = description,
            amount = amount,
            startDate = startDate,
            endDate = endDate,
            repeatInterval = repeatInterval,
            category = category
        )
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

        setupPaymentDialog(dialogBinding)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Editar pago programado")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                // Implementar actualización
                Toast.makeText(requireContext(), "Función de edición en desarrollo", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    private fun showDeletePaymentConfirmationDialog(payment: ScheduledPayment) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar pago programado")
            .setMessage("¿Estás seguro de que deseas eliminar este pago programado?")
            .setPositiveButton("Eliminar") { _, _ ->
                scheduledPaymentViewModel.deleteScheduledPayment(payment)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Savings Goals Dialogs
    private fun showAddSavingsGoalDialog() {
        val dialogBinding = DialogAddSavingsGoalBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Crear meta de ahorro")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                saveSavingsGoal(dialogBinding)
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    private fun saveSavingsGoal(dialogBinding: DialogAddSavingsGoalBinding) {
        val name = dialogBinding.nameInput.text.toString()
        val targetAmountText = dialogBinding.targetAmountInput.text.toString()

        if (name.isEmpty() || targetAmountText.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val targetAmount = targetAmountText.toDoubleOrNull()
        if (targetAmount == null || targetAmount <= 0) {
            Toast.makeText(requireContext(), "Por favor ingresa un monto válido", Toast.LENGTH_SHORT).show()
            return
        }

        val calendar = Calendar.getInstance()
        calendar.set(
            dialogBinding.targetDatePicker.year,
            dialogBinding.targetDatePicker.month,
            dialogBinding.targetDatePicker.dayOfMonth
        )
        val targetDate = calendar.time

        savingsGoalViewModel.addSavingsGoal(name, targetAmount, targetDate)
    }

    private fun showEditSavingsGoalDialog(goal: SavingsGoal) {
        val dialogBinding = DialogAddSavingsGoalBinding.inflate(layoutInflater)
        
        // Pre-llenar los campos
        dialogBinding.nameInput.setText(goal.name)
        dialogBinding.targetAmountInput.setText(goal.targetAmount.toString())
        
        val calendar = Calendar.getInstance().apply { time = goal.targetDate }
        dialogBinding.targetDatePicker.updateDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Editar meta de ahorro")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                // Implementar actualización
                Toast.makeText(requireContext(), "Función de edición en desarrollo", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    private fun showAddMoneyToGoalDialog(goal: SavingsGoal) {
        try {
            val dialogBinding = DialogAddMoneyToGoalBinding.inflate(layoutInflater)
            dialogBinding.goalNameText.text = goal.name
            dialogBinding.goalProgressText.text = "${goal.progress.toInt()}% completado - ${CurrencyUtils.formatAmount(requireContext(), goal.remainingAmount)} restante"

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Agregar dinero a la meta")
                .setView(dialogBinding.root)
                .setPositiveButton("Agregar") { _, _ ->
                    try {
                        val amountText = dialogBinding.amountInput.text.toString()
                        val amount = amountText.toDoubleOrNull()

                        if (amount == null || amount <= 0) {
                            Toast.makeText(requireContext(), "Por favor ingresa un monto válido", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        savingsGoalViewModel.addMoneyToGoal(goal.id, amount, goal.name)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error al procesar el monto: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .create()

            dialog.show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al mostrar el diálogo: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showDeleteSavingsGoalConfirmationDialog(goal: SavingsGoal) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar meta de ahorro")
            .setMessage("¿Estás seguro de que deseas eliminar esta meta de ahorro?")
            .setPositiveButton("Eliminar") { _, _ ->
                savingsGoalViewModel.deleteSavingsGoal(goal)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Category Limits Dialogs
    private fun showAddCategoryLimitDialog() {
        Log.d(TAG, "showAddCategoryLimitDialog: Iniciando")
        try {
            val dialogBinding = DialogAddCategoryLimitBinding.inflate(layoutInflater)
            Log.d(TAG, "showAddCategoryLimitDialog: Layout inflado correctamente")
            
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Crear límite de gasto")
                .setView(dialogBinding.root)
                .setPositiveButton("Guardar") { _, _ ->
                    Log.d(TAG, "showAddCategoryLimitDialog: Botón guardar clickeado")
                    saveCategoryLimit(dialogBinding)
                }
                .setNegativeButton("Cancelar", null)
                .create()

            setupCategoryLimitDialog(dialogBinding)
            Log.d(TAG, "showAddCategoryLimitDialog: Mostrando diálogo")
            dialog.show()
        } catch (e: Exception) {
            Log.e(TAG, "showAddCategoryLimitDialog: Error al mostrar diálogo", e)
            Toast.makeText(requireContext(), "Error al mostrar diálogo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCategoryLimitDialog(dialogBinding: DialogAddCategoryLimitBinding) {
        Log.d(TAG, "setupCategoryLimitDialog: Configurando diálogo")
        try {
            val categories = Categories.EXPENSE_CATEGORIES.map { it.name }
            (dialogBinding.categoryInput as? AutoCompleteTextView)?.setAdapter(
                ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
            )
            Log.d(TAG, "setupCategoryLimitDialog: Diálogo configurado correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "setupCategoryLimitDialog: Error al configurar diálogo", e)
        }
    }

    private fun saveCategoryLimit(dialogBinding: DialogAddCategoryLimitBinding) {
        Log.d(TAG, "saveCategoryLimit: Iniciando guardado")
        try {
            val category = dialogBinding.categoryInput.text.toString()
            val limitAmountText = dialogBinding.limitAmountInput.text.toString()

            Log.d(TAG, "saveCategoryLimit: Categoría: $category, Monto: $limitAmountText")

            if (category.isEmpty() || limitAmountText.isEmpty()) {
                Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return
            }

            val limitAmount = limitAmountText.toDoubleOrNull()
            if (limitAmount == null || limitAmount <= 0) {
                Toast.makeText(requireContext(), "Por favor ingresa un monto válido", Toast.LENGTH_SHORT).show()
                return
            }

            val startCalendar = Calendar.getInstance()
            startCalendar.set(
                dialogBinding.startDatePicker.year,
                dialogBinding.startDatePicker.month,
                dialogBinding.startDatePicker.dayOfMonth
            )
            val startDate = startCalendar.time

            val endCalendar = Calendar.getInstance()
            endCalendar.set(
                dialogBinding.endDatePicker.year,
                dialogBinding.endDatePicker.month,
                dialogBinding.endDatePicker.dayOfMonth
            )
            val endDate = endCalendar.time

            Log.d(TAG, "saveCategoryLimit: Llamando al ViewModel con datos: $category, $limitAmount, $startDate, $endDate")
            categoryLimitViewModel.addCategoryLimit(category, limitAmount, startDate, endDate)
        } catch (e: Exception) {
            Log.e(TAG, "saveCategoryLimit: Error al guardar límite", e)
            Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showEditCategoryLimitDialog(limit: CategoryLimit) {
        val dialogBinding = DialogAddCategoryLimitBinding.inflate(layoutInflater)
        
        // Pre-llenar los campos
        dialogBinding.categoryInput.setText(limit.category)
        dialogBinding.limitAmountInput.setText(limit.limitAmount.toString())
        
        val startCalendar = Calendar.getInstance().apply { time = limit.startDate }
        dialogBinding.startDatePicker.updateDate(
            startCalendar.get(Calendar.YEAR),
            startCalendar.get(Calendar.MONTH),
            startCalendar.get(Calendar.DAY_OF_MONTH)
        )
        
        val endCalendar = Calendar.getInstance().apply { time = limit.endDate }
        dialogBinding.endDatePicker.updateDate(
            endCalendar.get(Calendar.YEAR),
            endCalendar.get(Calendar.MONTH),
            endCalendar.get(Calendar.DAY_OF_MONTH)
        )

        setupCategoryLimitDialog(dialogBinding)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Editar límite de gasto")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                // Implementar actualización
                Toast.makeText(requireContext(), "Función de edición en desarrollo", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    private fun showDeleteCategoryLimitConfirmationDialog(limit: CategoryLimit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar límite de gasto")
            .setMessage("¿Estás seguro de que deseas eliminar este límite de gasto?")
            .setPositiveButton("Eliminar") { _, _ ->
                categoryLimitViewModel.deleteCategoryLimit(limit)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 