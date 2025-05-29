package com.example.ahorrapp.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.ahorrapp.R
import com.example.ahorrapp.data.AppDatabase
import com.example.ahorrapp.data.repository.UserRepository
import com.example.ahorrapp.databinding.FragmentLoginBinding
import com.example.ahorrapp.utils.SessionManager
import com.example.ahorrapp.viewmodel.AuthViewModel
import com.example.ahorrapp.viewmodel.AuthViewModelFactory

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            UserRepository(AppDatabase.getDatabase(requireContext()).userDao())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (validateInputs(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.registerLink.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = { user ->
                    // Guardar la sesión
                    sessionManager.saveSession(
                        userId = user.id,
                        email = user.email,
                        username = user.username
                    )
                    // Navegar a la pantalla principal
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                },
                onFailure = { exception ->
                    Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.emailLayout.error = "El email es requerido"
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        if (password.isEmpty()) {
            binding.passwordLayout.error = "La contraseña es requerida"
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 