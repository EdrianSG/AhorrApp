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
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.app.Activity
import android.content.res.Configuration

class SettingsFragment : Fragment() {
    private lateinit var currencySpinner: Spinner
    private lateinit var languageSpinner: Spinner
    private lateinit var themeRadioGroup: RadioGroup

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar vistas
        currencySpinner = view.findViewById(R.id.currencySpinner)
        languageSpinner = view.findViewById(R.id.languageSpinner)
        themeRadioGroup = view.findViewById(R.id.themeRadioGroup)

        // Configurar spinners
        setupCurrencySpinner()
        setupLanguageSpinner()
        setupThemeSelection()

        // Cargar preferencias guardadas
        loadSavedPreferences()

        // Retener la instancia del fragmento
        retainInstance = true
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
            
            // Actualizar la vista principal
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.supportFragmentManager.fragments.forEach { fragment ->
                    if (fragment is HomeFragment) {
                        fragment.updateBalance()
                        (fragment.view?.findViewById<RecyclerView>(R.id.transactionsList)?.adapter as? TransactionAdapter)?.refreshAmounts()
                    }
                }
            }
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
            val mode = when (checkedId) {
                R.id.lightThemeRadio -> AppCompatDelegate.MODE_NIGHT_NO
                R.id.darkThemeRadio -> AppCompatDelegate.MODE_NIGHT_YES
                R.id.systemThemeRadio -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            
            // Guardar la preferencia antes de cambiar el tema
            savePreference("theme_mode", mode.toString())
            
            // Aplicar el cambio de tema sin recrear la actividad
            activity?.let { act ->
                AppCompatDelegate.setDefaultNightMode(mode)
                // Forzar la actualización de la UI sin recrear la actividad
                act.window.decorView.systemUiVisibility = if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
                    0
                } else {
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }
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