package com.example.ahorrapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.ahorrapp.data.model.Wallet
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {

    @Insert
    suspend fun insertWallet(wallet: Wallet): Long

    @Update
    suspend fun updateWallet(wallet: Wallet)

    @Delete
    suspend fun deleteWallet(wallet: Wallet)

    @Query("SELECT * FROM wallets WHERE userId = :userId ORDER BY id")
    fun getWalletsByUser(userId: Long): Flow<List<Wallet>>

    @Query("SELECT * FROM wallets WHERE userId = :userId AND isActive = 1 ORDER BY id")
    suspend fun getActiveWalletsByUserOnce(userId: Long): List<Wallet>

    @Query("SELECT * FROM wallets WHERE id = :walletId")
    suspend fun getWalletById(walletId: Long): Wallet?
}






