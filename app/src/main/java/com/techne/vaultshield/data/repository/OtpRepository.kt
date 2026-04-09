package com.techne.vaultshield.data.repository

import com.techne.vaultshield.domain.model.OtpAccount
import kotlinx.coroutines.flow.Flow

interface OtpRepository {
    fun getAccounts(): Flow<List<OtpAccount>>
    suspend fun addAccount(account: OtpAccount)
    suspend fun deleteAccount(id: String)
}