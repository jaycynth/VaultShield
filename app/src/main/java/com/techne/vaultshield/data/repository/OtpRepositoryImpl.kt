package com.techne.vaultshield.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.techne.vaultshield.data.local.OtpDao
import com.techne.vaultshield.data.local.OtpEntity
import com.techne.vaultshield.domain.model.OtpAccount
import com.techne.vaultshield.domain.model.OtpRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtpRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val otpDao: OtpDao
) : OtpRepository {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedSecrets = EncryptedSharedPreferences.create(
        context,
        "secure_otp_secrets",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun getAccounts(): Flow<List<OtpAccount>> {
        return otpDao.getAllAccounts().map { entities ->
            entities.map { entity ->
                val secret = encryptedSecrets.getString(entity.id, "") ?: ""
                entity.toDomain(secret)
            }
        }
    }

    override suspend fun addAccount(account: OtpAccount) {
        // 1. Store non-sensitive metadata in Room
        otpDao.insertAccount(OtpEntity.fromDomain(account))
        
        // 2. Store sensitive secret in EncryptedSharedPreferences (Keystore-backed)
        encryptedSecrets.edit().putString(account.id, account.secret).apply()
    }

    override suspend fun deleteAccount(id: String) {
        // 1. Remove metadata
        otpDao.deleteAccount(id)
        
        // 2. Remove secret
        encryptedSecrets.edit().remove(id).apply()
    }
}
