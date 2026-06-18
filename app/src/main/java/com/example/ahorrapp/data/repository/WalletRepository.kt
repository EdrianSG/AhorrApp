package com.example.ahorrapp.data.repository

import com.example.ahorrapp.data.dao.WalletDao
import com.example.ahorrapp.data.model.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class WalletRepository(private val walletDao: WalletDao) {

    fun getWalletsByUser(userId: Long): Flow<List<Wallet>> {
        return walletDao.getWalletsByUser(userId)
    }

    suspend fun getActiveWalletsByUserOnce(userId: Long): List<Wallet> {
        return withContext(Dispatchers.IO) {
            walletDao.getActiveWalletsByUserOnce(userId)
        }
    }

    suspend fun addWallet(wallet: Wallet): Result<Wallet> {
        return withContext(Dispatchers.IO) {
            try {
                val id = walletDao.insertWallet(wallet)
                Result.success(wallet.copy(id = id))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun updateWallet(wallet: Wallet): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                walletDao.updateWallet(wallet)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteWallet(wallet: Wallet): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                walletDao.deleteWallet(wallet)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}






