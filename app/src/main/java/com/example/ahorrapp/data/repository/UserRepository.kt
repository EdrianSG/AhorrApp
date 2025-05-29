package com.example.ahorrapp.data.repository

import com.example.ahorrapp.data.dao.UserDao
import com.example.ahorrapp.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(username: String, email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                // Verificar si el usuario ya existe
                if (userDao.getUserByEmail(email) != null) {
                    return@withContext Result.failure(Exception("El email ya está registrado"))
                }
                if (userDao.getUserByUsername(username) != null) {
                    return@withContext Result.failure(Exception("El nombre de usuario ya está en uso"))
                }

                // Crear nuevo usuario
                val user = User(
                    username = username,
                    email = email,
                    password = password
                )
                val id = userDao.insertUser(user)
                Result.success(user.copy(id = id))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val user = userDao.validateUser(email, password)
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Credenciales inválidas"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
} 