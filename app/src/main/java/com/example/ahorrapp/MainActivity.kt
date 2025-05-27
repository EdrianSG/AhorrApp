package com.example.ahorrapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.ahorrapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentNavigationId = R.id.navigation_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Restaurar el estado de navegación si existe
        currentNavigationId = savedInstanceState?.getInt("current_navigation", R.id.navigation_home)
            ?: R.id.navigation_home

        // Cargar el fragmento inicial o restaurar el anterior
        if (savedInstanceState == null) {
            loadFragment(getFragmentForNavigation(currentNavigationId))
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            currentNavigationId = item.itemId
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.navigation_dashboard -> {
                    loadFragment(DashboardFragment())
                    true
                }
                R.id.navigation_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }

        // Seleccionar el item guardado
        binding.bottomNavigation.selectedItemId = currentNavigationId
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("current_navigation", currentNavigationId)
    }

    private fun getFragmentForNavigation(navigationId: Int): Fragment {
        return when (navigationId) {
            R.id.navigation_dashboard -> DashboardFragment()
            R.id.navigation_settings -> SettingsFragment()
            else -> HomeFragment()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}