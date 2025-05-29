package com.example.ahorrapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.ahorrapp.databinding.ActivityMainBinding
import com.example.ahorrapp.utils.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Configurar el NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

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

        // Verificar si hay una sesión activa
        if (sessionManager.isLoggedIn()) {
            navController.navigate(R.id.action_loginFragment_to_homeFragment)
        }
    }
}