package com.techne.vaultshield.security

import com.techne.vaultshield.domain.model.OtpRepository
import com.techne.vaultshield.domain.model.OtpAccount
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    private val repository: OtpRepository
) {
    /**
     * Exports all accounts to an encrypted string.
     */
    suspend fun createEncryptedBackup(password: CharArray): String {
        val accounts = repository.getAccounts().first()
        val jsonArray = JSONArray()
        accounts.forEach { account ->
            val obj = JSONObject().apply {
                put("issuer", account.issuer)
                put("accountName", account.accountName)
                put("secret", account.secret)
                put("digits", account.digits)
                put("period", account.period)
                put("algorithm", account.algorithm)
            }
            jsonArray.put(obj)
        }
        
        return EncryptionManager.encryptWithPassword(jsonArray.toString(), password)
    }

    /**
     * Restores accounts from an encrypted string.
     */
    suspend fun restoreFromBackup(encryptedData: String, password: CharArray): Result<Int> {
        return try {
            val decryptedJson = EncryptionManager.decryptWithPassword(encryptedData, password)
            val jsonArray = JSONArray(decryptedJson)
            var count = 0
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val account = OtpAccount(
                    id = java.util.UUID.randomUUID().toString(),
                    issuer = obj.getString("issuer"),
                    accountName = obj.getString("accountName"),
                    secret = obj.getString("secret"),
                    digits = obj.getInt("digits"),
                    period = obj.getInt("period"),
                    algorithm = obj.getString("algorithm")
                )
                repository.addAccount(account)
                count++
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
