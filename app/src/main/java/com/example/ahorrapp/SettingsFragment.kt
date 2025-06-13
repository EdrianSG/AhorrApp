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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import android.app.Activity
import android.content.res.Configuration
import com.example.ahorrapp.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.ahorrapp.data.AppDatabase
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.Intent

class SettingsFragment : Fragment() {
    private lateinit var currencySpinner: Spinner
    private lateinit var languageSpinner: Spinner
    private lateinit var themeRadioGroup: RadioGroup
    private lateinit var logoutButton: MaterialButton
    private lateinit var userNameText: TextView
    private lateinit var sessionManager: SessionManager
    private var isThemeChanging = false

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
        logoutButton = view.findViewById(R.id.logoutButton)
        userNameText = view.findViewById(R.id.userNameText)

        // Configurar spinners y tema
        setupCurrencySpinner()
        setupLanguageSpinner()
        setupThemeSelection()
        setupLogoutButton()
        loadUserData()

        // Cargar preferencias guardadas
        loadSavedPreferences()
    }

    private fun loadUserData() {
        val userId = sessionManager.getUserId()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userDao = AppDatabase.getDatabase(requireContext()).userDao()
                val user = userDao.getUserById(userId)
                user?.let {
                    userNameText.text = it.username
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupLogoutButton() {
        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
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
            if (!isThemeChanging) {
                isThemeChanging = true
                when (checkedId) {
                    R.id.lightThemeRadio -> setTheme(AppCompatDelegate.MODE_NIGHT_NO)
                    R.id.darkThemeRadio -> setTheme(AppCompatDelegate.MODE_NIGHT_YES)
                    R.id.systemThemeRadio -> setTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
        }
    }

    private fun setTheme(mode: Int) {
        try {
            savePreference("theme_mode", mode.toString())
            AppCompatDelegate.setDefaultNightMode(mode)
        } catch (e: Exception) {
            Snackbar.make(
                requireView(),
                "Error al cambiar el tema: ${e.message}",
                Snackbar.LENGTH_LONG
            ).show()
        } finally {
            isThemeChanging = false
        }
    }

    private fun loadSavedPreferences() {
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        
        // Cargar moneda
        val savedCurrency = prefs.getString("currency", "USD")
        val currencyValues = resources.getStringArray(R.array.currencies_values)
        currencySpinner.setSelection(currencyValues.indexOf(savedCurrency))

        // Cargar idioma
        val savedLanguage = prefs.getString("language", "es")
        val languageValues = resources.getStringArray(R.array.languages_values)
        languageSpinner.setSelection(languageValues.indexOf(savedLanguage))

        // Cargar tema
        val savedThemeMode = prefs.getString("theme_mode", 
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString())?.toInt()
        val radioButtonId = when (savedThemeMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> R.id.lightThemeRadio
            AppCompatDelegate.MODE_NIGHT_YES -> R.id.darkThemeRadio
            else -> R.id.systemThemeRadio
        }
        themeRadioGroup.check(radioButtonId)
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