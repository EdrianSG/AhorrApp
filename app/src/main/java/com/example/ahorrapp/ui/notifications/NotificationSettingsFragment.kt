package com.example.ahorrapp.ui.notifications

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.ahorrapp.R
import com.example.ahorrapp.utils.SoundUtils
import com.example.ahorrapp.viewmodel.NotificationSettingsViewModel
import com.google.android.material.switchmaterial.SwitchMaterial
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class NotificationSettingsFragment : Fragment() {

    private val viewModel: NotificationSettingsViewModel by viewModels()

    // Switches para pagos programados
    private lateinit var scheduledPaymentsSwitch: SwitchMaterial
    private lateinit var scheduledPaymentsSoundSwitch: SwitchMaterial
    private lateinit var scheduledPaymentsSoundSpinner: Spinner
    private lateinit var scheduledPaymentsVibrationSwitch: SwitchMaterial
    private lateinit var scheduledPaymentsAdvanceTimeText: TextView
    private lateinit var scheduledPaymentsAdvanceTimeSeekBar: SeekBar

    // Switches para metas de ahorro
    private lateinit var savingsGoalsSwitch: SwitchMaterial
    private lateinit var savingsGoalsSoundSwitch: SwitchMaterial
    private lateinit var savingsGoalsSoundSpinner: Spinner
    private lateinit var savingsGoalsVibrationSwitch: SwitchMaterial

    // Switches para límites de categoría
    private lateinit var categoryLimitsSwitch: SwitchMaterial
    private lateinit var categoryLimitsSoundSwitch: SwitchMaterial
    private lateinit var categoryLimitsSoundSpinner: Spinner
    private lateinit var categoryLimitsVibrationSwitch: SwitchMaterial
    private lateinit var categoryLimitsThresholdText: TextView
    private lateinit var categoryLimitsThresholdSeekBar: SeekBar

    // Switches para notificaciones generales
    private lateinit var generalNotificationsSwitch: SwitchMaterial
    private lateinit var generalNotificationsSoundSwitch: SwitchMaterial
    private lateinit var generalNotificationsSoundSpinner: Spinner
    private lateinit var generalNotificationsVibrationSwitch: SwitchMaterial

    // Switches para horas silenciosas
    private lateinit var quietHoursSwitch: SwitchMaterial
    private lateinit var quietHoursStartText: TextView
    private lateinit var quietHoursEndText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupSoundSpinners()
        setupListeners()
        observeViewModel()
    }

    private fun initializeViews(view: View) {
        // Pagos programados
        scheduledPaymentsSwitch = view.findViewById(R.id.scheduledPaymentsSwitch)
        scheduledPaymentsSoundSwitch = view.findViewById(R.id.scheduledPaymentsSoundSwitch)
        scheduledPaymentsSoundSpinner = view.findViewById(R.id.scheduledPaymentsSoundSpinner)
        scheduledPaymentsVibrationSwitch = view.findViewById(R.id.scheduledPaymentsVibrationSwitch)
        scheduledPaymentsAdvanceTimeText = view.findViewById(R.id.scheduledPaymentsAdvanceTimeText)
        scheduledPaymentsAdvanceTimeSeekBar = view.findViewById(R.id.scheduledPaymentsAdvanceTimeSeekBar)

        // Metas de ahorro
        savingsGoalsSwitch = view.findViewById(R.id.savingsGoalsSwitch)
        savingsGoalsSoundSwitch = view.findViewById(R.id.savingsGoalsSoundSwitch)
        savingsGoalsSoundSpinner = view.findViewById(R.id.savingsGoalsSoundSpinner)
        savingsGoalsVibrationSwitch = view.findViewById(R.id.savingsGoalsVibrationSwitch)

        // Límites de categoría
        categoryLimitsSwitch = view.findViewById(R.id.categoryLimitsSwitch)
        categoryLimitsSoundSwitch = view.findViewById(R.id.categoryLimitsSoundSwitch)
        categoryLimitsSoundSpinner = view.findViewById(R.id.categoryLimitsSoundSpinner)
        categoryLimitsVibrationSwitch = view.findViewById(R.id.categoryLimitsVibrationSwitch)
        categoryLimitsThresholdText = view.findViewById(R.id.categoryLimitsThresholdText)
        categoryLimitsThresholdSeekBar = view.findViewById(R.id.categoryLimitsThresholdSeekBar)

        // Notificaciones generales
        generalNotificationsSwitch = view.findViewById(R.id.generalNotificationsSwitch)
        generalNotificationsSoundSwitch = view.findViewById(R.id.generalNotificationsSoundSwitch)
        generalNotificationsSoundSpinner = view.findViewById(R.id.generalNotificationsSoundSpinner)
        generalNotificationsVibrationSwitch = view.findViewById(R.id.generalNotificationsVibrationSwitch)

        // Horas silenciosas
        quietHoursSwitch = view.findViewById(R.id.quietHoursSwitch)
        quietHoursStartText = view.findViewById(R.id.quietHoursStartText)
        quietHoursEndText = view.findViewById(R.id.quietHoursEndText)
    }

    private fun setupSoundSpinners() {
        val soundOptions = SoundUtils.getSystemSoundsList()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, soundOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        scheduledPaymentsSoundSpinner.adapter = adapter
        savingsGoalsSoundSpinner.adapter = adapter
        categoryLimitsSoundSpinner.adapter = adapter
        generalNotificationsSoundSpinner.adapter = adapter
    }

    private fun setupListeners() {
        // Pagos programados
        scheduledPaymentsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateScheduledPaymentsEnabled(isChecked)
        }

        scheduledPaymentsSoundSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateScheduledPaymentsSound(isChecked)
        }

        scheduledPaymentsSoundSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val soundName = SoundUtils.getSystemSoundsList()[position]
                val soundKey = SoundUtils.getSystemSoundKey(soundName)
                viewModel.updateScheduledPaymentsSoundUri("system:$soundKey")
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        scheduledPaymentsVibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateScheduledPaymentsVibration(isChecked)
        }

        scheduledPaymentsAdvanceTimeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val minutes = progress + 5 // Mínimo 5 minutos
                    scheduledPaymentsAdvanceTimeText.text = "${minutes} minutos antes"
                    viewModel.updateScheduledPaymentsAdvanceTime(minutes)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Metas de ahorro
        savingsGoalsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateSavingsGoalsEnabled(isChecked)
        }

        savingsGoalsSoundSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateSavingsGoalsSound(isChecked)
        }

        savingsGoalsSoundSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val soundName = SoundUtils.getSystemSoundsList()[position]
                val soundKey = SoundUtils.getSystemSoundKey(soundName)
                viewModel.updateSavingsGoalsSoundUri("system:$soundKey")
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        savingsGoalsVibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateSavingsGoalsVibration(isChecked)
        }

        // Límites de categoría
        categoryLimitsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateCategoryLimitsEnabled(isChecked)
        }

        categoryLimitsSoundSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateCategoryLimitsSound(isChecked)
        }

        categoryLimitsSoundSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val soundName = SoundUtils.getSystemSoundsList()[position]
                val soundKey = SoundUtils.getSystemSoundKey(soundName)
                viewModel.updateCategoryLimitsSoundUri("system:$soundKey")
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        categoryLimitsVibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateCategoryLimitsVibration(isChecked)
        }

        categoryLimitsThresholdSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val threshold = progress + 70 // Mínimo 70%
                    categoryLimitsThresholdText.text = "${threshold}% del límite"
                    viewModel.updateCategoryLimitsThreshold(threshold)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Notificaciones generales
        generalNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateGeneralNotificationsEnabled(isChecked)
        }

        generalNotificationsSoundSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateGeneralNotificationsSound(isChecked)
        }

        generalNotificationsSoundSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val soundName = SoundUtils.getSystemSoundsList()[position]
                val soundKey = SoundUtils.getSystemSoundKey(soundName)
                viewModel.updateGeneralNotificationsSoundUri("system:$soundKey")
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        generalNotificationsVibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateGeneralNotificationsVibration(isChecked)
        }

        // Horas silenciosas
        quietHoursSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateQuietHoursEnabled(isChecked)
        }

        quietHoursStartText.setOnClickListener {
            showTimePickerDialog { time ->
                viewModel.updateQuietHoursStart(time)
            }
        }

        quietHoursEndText.setOnClickListener {
            showTimePickerDialog { time ->
                viewModel.updateQuietHoursEnd(time)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.settings.collect { settings ->
                settings?.let { updateUI(it) }
            }
        }
    }

    private fun updateUI(settings: com.example.ahorrapp.data.model.NotificationSettings) {
        // Pagos programados
        scheduledPaymentsSwitch.isChecked = settings.scheduledPaymentsEnabled
        scheduledPaymentsSoundSwitch.isChecked = settings.scheduledPaymentsSound
        scheduledPaymentsVibrationSwitch.isChecked = settings.scheduledPaymentsVibration
        scheduledPaymentsAdvanceTimeSeekBar.progress = settings.scheduledPaymentsAdvanceTime - 5
        scheduledPaymentsAdvanceTimeText.text = "${settings.scheduledPaymentsAdvanceTime} minutos antes"
        
        // Configurar spinner de sonido para pagos programados
        val scheduledPaymentSoundKey = if (settings.scheduledPaymentsSoundUri.startsWith("system:")) {
            settings.scheduledPaymentsSoundUri.substring(7)
        } else {
            "default"
        }
        val scheduledPaymentSoundName = SoundUtils.getSystemSoundDisplayName(scheduledPaymentSoundKey)
        val scheduledPaymentSoundIndex = SoundUtils.getSystemSoundsList().indexOf(scheduledPaymentSoundName)
        if (scheduledPaymentSoundIndex >= 0) {
            scheduledPaymentsSoundSpinner.setSelection(scheduledPaymentSoundIndex)
        }

        // Metas de ahorro
        savingsGoalsSwitch.isChecked = settings.savingsGoalsEnabled
        savingsGoalsSoundSwitch.isChecked = settings.savingsGoalsSound
        savingsGoalsVibrationSwitch.isChecked = settings.savingsGoalsVibration
        
        // Configurar spinner de sonido para metas de ahorro
        val savingsGoalSoundKey = if (settings.savingsGoalsSoundUri.startsWith("system:")) {
            settings.savingsGoalsSoundUri.substring(7)
        } else {
            "default"
        }
        val savingsGoalSoundName = SoundUtils.getSystemSoundDisplayName(savingsGoalSoundKey)
        val savingsGoalSoundIndex = SoundUtils.getSystemSoundsList().indexOf(savingsGoalSoundName)
        if (savingsGoalSoundIndex >= 0) {
            savingsGoalsSoundSpinner.setSelection(savingsGoalSoundIndex)
        }

        // Límites de categoría
        categoryLimitsSwitch.isChecked = settings.categoryLimitsEnabled
        categoryLimitsSoundSwitch.isChecked = settings.categoryLimitsSound
        categoryLimitsVibrationSwitch.isChecked = settings.categoryLimitsVibration
        categoryLimitsThresholdSeekBar.progress = settings.categoryLimitsThreshold - 70
        categoryLimitsThresholdText.text = "${settings.categoryLimitsThreshold}% del límite"
        
        // Configurar spinner de sonido para límites de categoría
        val categoryLimitSoundKey = if (settings.categoryLimitsSoundUri.startsWith("system:")) {
            settings.categoryLimitsSoundUri.substring(7)
        } else {
            "default"
        }
        val categoryLimitSoundName = SoundUtils.getSystemSoundDisplayName(categoryLimitSoundKey)
        val categoryLimitSoundIndex = SoundUtils.getSystemSoundsList().indexOf(categoryLimitSoundName)
        if (categoryLimitSoundIndex >= 0) {
            categoryLimitsSoundSpinner.setSelection(categoryLimitSoundIndex)
        }

        // Notificaciones generales
        generalNotificationsSwitch.isChecked = settings.generalNotificationsEnabled
        generalNotificationsSoundSwitch.isChecked = settings.generalNotificationsSound
        generalNotificationsVibrationSwitch.isChecked = settings.generalNotificationsVibration
        
        // Configurar spinner de sonido para notificaciones generales
        val generalNotificationSoundKey = if (settings.generalNotificationsSoundUri.startsWith("system:")) {
            settings.generalNotificationsSoundUri.substring(7)
        } else {
            "default"
        }
        val generalNotificationSoundName = SoundUtils.getSystemSoundDisplayName(generalNotificationSoundKey)
        val generalNotificationSoundIndex = SoundUtils.getSystemSoundsList().indexOf(generalNotificationSoundName)
        if (generalNotificationSoundIndex >= 0) {
            generalNotificationsSoundSpinner.setSelection(generalNotificationSoundIndex)
        }

        // Horas silenciosas
        quietHoursSwitch.isChecked = settings.quietHoursEnabled
        quietHoursStartText.text = settings.quietHoursStart
        quietHoursEndText.text = settings.quietHoursEnd
    }

    private fun showTimePickerDialog(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val time = String.format("%02d:%02d", selectedHour, selectedMinute)
                onTimeSelected(time)
            },
            hour,
            minute,
            true
        ).show()
    }
} 