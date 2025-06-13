package com.example.ahorrapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.ahorrapp.databinding.ActivityMainBinding
import com.example.ahorrapp.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Verificar si hay una sesión activa
        if (!sessionManager.isLoggedIn()) {
            // Si no hay sesión, navegar al login
            navController.navigate(R.id.loginFragment)
        }

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
}