package com.example.ahorrapp.data.dao

import androidx.room.*
import com.example.ahorrapp.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun validateUser(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): User?

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)
} 