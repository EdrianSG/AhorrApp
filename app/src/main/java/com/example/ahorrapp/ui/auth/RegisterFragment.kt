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
import com.example.ahorrapp.databinding.FragmentRegisterBinding
import com.example.ahorrapp.viewmodel.AuthViewModel
import com.example.ahorrapp.viewmodel.AuthViewModelFactory

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.registerButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (validateInputs(username, email, password, confirmPassword)) {
                viewModel.register(username, email, password)
            }
        }

        binding.loginLink.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.registerResult.observe(viewLifecycleOwner) { result ->
            result.fold(
                onSuccess = {
                    Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                },
                onFailure = { exception ->
                    Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun validateInputs(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        if (username.isEmpty()) {
            binding.usernameLayout.error = "El nombre de usuario es requerido"
            isValid = false
        } else {
            binding.usernameLayout.error = null
        }

        if (email.isEmpty()) {
            binding.emailLayout.error = "El email es requerido"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Email inválido"
            isValid = false
        } else {
            binding.emailLayout.error = null
        }

        if (password.isEmpty()) {
            binding.passwordLayout.error = "La contraseña es requerida"
            isValid = false
        } else if (password.length < 6) {
            binding.passwordLayout.error = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        } else {
            binding.passwordLayout.error = null
        }

        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordLayout.error = "Confirma tu contraseña"
            isValid = false
        } else if (password != confirmPassword) {
            binding.confirmPasswordLayout.error = "Las contraseñas no coinciden"
            isValid = false
        } else {
            binding.confirmPasswordLayout.error = null
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 