package com.example.ahorrapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import android.content.res.Configuration
import com.example.ahorrapp.utils.SessionManager
import com.example.ahorrapp.utils.ThemeUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.ahorrapp.data.AppDatabase
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class SettingsFragment : Fragment() {
    private lateinit var currencySpinner: Spinner
    private lateinit var languageSpinner: Spinner
    private lateinit var themeRadioGroup: RadioGroup
    private lateinit var colorThemeSpinner: Spinner
    private lateinit var logoutButton: MaterialButton
    private lateinit var notificationSettingsButton: MaterialButton
    private lateinit var walletsButton: MaterialButton
    private lateinit var userNameText: TextView
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // Inicializar vistas
        currencySpinner = view.findViewById(R.id.currencySpinner)
        languageSpinner = view.findViewById(R.id.languageSpinner)
        themeRadioGroup = view.findViewById(R.id.themeRadioGroup)
        colorThemeSpinner = view.findViewById(R.id.colorThemeSpinner)
        logoutButton = view.findViewById(R.id.logoutButton)
        notificationSettingsButton = view.findViewById(R.id.notificationSettingsButton)
        walletsButton = view.findViewById(R.id.walletsButton)
        userNameText = view.findViewById(R.id.userNameText)

        // Configurar spinners y tema
        setupCurrencySpinner()
        setupLanguageSpinner()
        setupThemeSelection()
        setupColorThemeSelection()
        setupLogoutButton()
        setupNotificationSettingsButton()
        setupWalletsButton()
        loadUserData()

        // Cargar preferencias guardadas
        loadSavedPreferences()
    }

    private fun loadUserData() {
        val userId = sessionManager.getUserId()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
            val userDao = AppDatabase.getDatabase(requireContext()).userDao()
            val user = userDao.getUserById(userId)
                    withContext(Dispatchers.Main) {
            user?.let {
                userNameText.text = it.username
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupLogoutButton() {
        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun setupNotificationSettingsButton() {
        notificationSettingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_notificationSettingsFragment)
        }
    }

    private fun setupWalletsButton() {
        walletsButton.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_walletsFragment)
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun logout() {
        sessionManager.clearSession()
        findNavController().navigate(R.id.action_settingsFragment_to_loginFragment)
    }

    private fun setupCurrencySpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.currencies,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            currencySpinner.adapter = adapter
        }

        currencySpinner.setOnItemSelectedListener { position ->
            val currencies = resources.getStringArray(R.array.currencies_values)
            savePreference("currency", currencies[position])
            
            // Notificar el cambio de moneda
            LocalBroadcastManager.getInstance(requireContext())
                .sendBroadcast(Intent("com.example.ahorrapp.CURRENCY_CHANGED"))
        }
    }

    private fun setupLanguageSpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.languages,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            languageSpinner.adapter = adapter
        }

        languageSpinner.setOnItemSelectedListener { position ->
            val languages = resources.getStringArray(R.array.languages_values)
            savePreference("language", languages[position])
        }
    }

    private fun setupThemeSelection() {
        themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                R.id.lightThemeRadio -> applyTheme(AppCompatDelegate.MODE_NIGHT_NO)
                R.id.darkThemeRadio -> applyTheme(AppCompatDelegate.MODE_NIGHT_YES)
                R.id.systemThemeRadio -> applyTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    private fun setupColorThemeSelection() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.color_themes,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            colorThemeSpinner.adapter = adapter
        }

        colorThemeSpinner.setOnItemSelectedListener { position ->
            val colorThemes = resources.getStringArray(R.array.color_themes_values)
            val selectedColor = colorThemes[position]
            applyColorTheme(selectedColor)
        }
    }

    private fun applyColorTheme(colorTheme: String) {
        try {
            val savedColor = ThemeUtils.getSavedColorTheme(requireContext())
            if (savedColor != colorTheme) {
                ThemeUtils.applyColorTheme(requireContext(), colorTheme)
                
                // Actualizar los colores dinámicos en colors.xml
                updateDynamicColorsInXml(colorTheme)
                
                // Recrear la actividad para aplicar los nuevos colores
                view?.postDelayed({
                    activity?.recreate()
                }, 100)
            }
        } catch (e: Exception) {
            view?.let {
                Snackbar.make(
                    it,
                    "Error al cambiar el color: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun updateDynamicColorsInXml(colorTheme: String) {
        // No podemos modificar XML en tiempo de ejecución porque los recursos están compilados
        // En su lugar, los colores se actualizarán cuando se recree la actividad
        // El sistema leerá la preferencia guardada y aplicará los colores correctos
    }

    private fun applyTheme(mode: Int) {
        try {
            val currentMode = AppCompatDelegate.getDefaultNightMode()
            if (currentMode != mode) {
            savePreference("theme_mode", mode.toString())

                // Aplicar inmediatamente y recrear para efecto instantáneo
                AppCompatDelegate.setDefaultNightMode(mode)
                (activity as? AppCompatActivity)?.delegate?.applyDayNight()
                activity?.recreate()
            }
        } catch (e: Exception) {
            view?.let {
            Snackbar.make(
                    it,
                "Error al cambiar el tema: ${e.message}",
                Snackbar.LENGTH_LONG
            ).show()
            }
        }
    }

    private fun loadSavedPreferences() {
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        
        // Cargar moneda
        val savedCurrency = prefs.getString("currency", "USD")
        val currencyValues = resources.getStringArray(R.array.currencies_values)
        val currencyIndex = currencyValues.indexOf(savedCurrency)
        if (currencyIndex >= 0) {
            currencySpinner.setSelection(currencyIndex)
        }

        // Cargar idioma
        val savedLanguage = prefs.getString("language", "es")
        val languageValues = resources.getStringArray(R.array.languages_values)
        val languageIndex = languageValues.indexOf(savedLanguage)
        if (languageIndex >= 0) {
            languageSpinner.setSelection(languageIndex)
        }

        // Cargar tema
        val savedThemeMode = prefs.getString("theme_mode", 
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString())?.toIntOrNull()
        val radioButtonId = when (savedThemeMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> R.id.lightThemeRadio
            AppCompatDelegate.MODE_NIGHT_YES -> R.id.darkThemeRadio
            else -> R.id.systemThemeRadio
        }
        themeRadioGroup.check(radioButtonId)

        // Cargar color de tema
        val savedColorTheme = ThemeUtils.getSavedColorTheme(requireContext())
        val colorThemeValues = resources.getStringArray(R.array.color_themes_values)
        val colorThemeIndex = colorThemeValues.indexOf(savedColorTheme)
        if (colorThemeIndex >= 0) {
            colorThemeSpinner.setSelection(colorThemeIndex)
        }
    }

    private fun savePreference(key: String, value: String) {
        requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
            .edit()
            .putString(key, value)
            .apply()
    }

    private fun Spinner.setOnItemSelectedListener(onItemSelected: (Int) -> Unit) {
        this.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                onItemSelected(position)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }
} 