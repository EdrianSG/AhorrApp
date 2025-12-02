package com.example.ahorrapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.ahorrapp.databinding.ActivityMainBinding
import com.example.ahorrapp.utils.SessionManager
import com.example.ahorrapp.utils.ThemeUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatDelegate
import android.content.Context
import androidx.navigation.NavController
import android.content.res.Resources
import android.content.res.Configuration
import java.util.Locale
import java.io.File

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Aplicar el tema y color antes de setContentView
        applyTheme()
        applyColorTheme()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }


    private fun setupNavigation() {
        // Configurar el NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Configurar el destino inicial basado en el estado de la sesión
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        navGraph.setStartDestination(
            if (sessionManager.isLoggedIn()) R.id.navigation_home
            else R.id.loginFragment
        )
        navController.graph = navGraph

        // Configurar la navegación inferior
        binding.bottomNavigation.setupWithNavController(navController)

        // Ocultar/mostrar la barra de navegación según la pantalla
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment -> {
                    binding.bottomNavigation.visibility = android.view.View.GONE
                }
                else -> {
                    binding.bottomNavigation.visibility = android.view.View.VISIBLE
                }
            }
        }

        // Prevenir navegación al login si ya hay sesión activa
        navController.addOnDestinationChangedListener { controller, destination, _ ->
            if (sessionManager.isLoggedIn() && 
                (destination.id == R.id.loginFragment || destination.id == R.id.registerFragment)) {
                controller.navigateUp()
            }
        }
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val savedThemeMode = prefs.getString("theme_mode", 
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString())?.toIntOrNull()
            ?: AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            
        if (AppCompatDelegate.getDefaultNightMode() != savedThemeMode) {
            AppCompatDelegate.setDefaultNightMode(savedThemeMode)
        }
    }

    private fun applyColorTheme() {
        val colorTheme = ThemeUtils.getSavedColorTheme(this)
        // Aplicar el tema correcto según el color seleccionado
        val themeResId = when (colorTheme) {
            "blue" -> R.style.Theme_AhorrApp_Blue
            "red" -> R.style.Theme_AhorrApp_Red
            "purple" -> R.style.Theme_AhorrApp_Purple
            "orange" -> R.style.Theme_AhorrApp_Orange
            "pink" -> R.style.Theme_AhorrApp_Pink
            else -> R.style.Theme_AhorrApp_Green
        }
        setTheme(themeResId)
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        // No es necesario hacer nada aquí, Android manejará el cambio de configuración
    }
}