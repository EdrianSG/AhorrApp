package com.example.ahorrapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ahorrapp.data.model.User
import com.example.ahorrapp.data.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<User>>()
    val loginResult: LiveData<Result<User>> = _loginResult

    private val _registerResult = MutableLiveData<Result<Boolean>>()
    val registerResult: LiveData<Result<Boolean>> = _registerResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = userRepository.loginUser(email, password)
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            val result = userRepository.registerUser(username, email, password)
            _registerResult.value = result.map { true }
        }
    }
} 