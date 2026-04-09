package com.techne.vaultshield.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.techne.vaultshield.domain.model.OtpAccount
import com.techne.vaultshield.domain.model.OtpRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtpRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : OtpRepository {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_otp_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _accounts = MutableStateFlow<List<OtpAccount>>(emptyList())
    
    init {
        loadAccounts()
    }

    override fun getAccounts(): Flow<List<OtpAccount>> = _accounts.asStateFlow()

    override suspend fun addAccount(account: OtpAccount) {
        val current = _accounts.value.toMutableList()
        current.add(account)
        saveAccounts(current)
        _accounts.value = current
    }

    override suspend fun deleteAccount(id: String) {
        val current = _accounts.value.filter { it.id != id }
        saveAccounts(current)
        _accounts.value = current
    }

    private fun loadAccounts() {
        val jsonString = sharedPreferences.getString("accounts", "[]") ?: "[]"
        val jsonArray = JSONArray(jsonString)
        val list = mutableListOf<OtpAccount>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                OtpAccount(
                    id = obj.getString("id"),
                    issuer = obj.getString("issuer"),
                    accountName = obj.getString("accountName"),
                    secret = obj.getString("secret"),
                    digits = obj.getInt("digits"),
                    period = obj.getInt("period"),
                    algorithm = obj.getString("algorithm")
                )
            )
        }
        _accounts.value = list
    }

    private fun saveAccounts(accounts: List<OtpAccount>) {
        val jsonArray = JSONArray()
        accounts.forEach { account ->
            val obj = JSONObject().apply {
                put("id", account.id)
                put("issuer", account.issuer)
                put("accountName", account.accountName)
                put("secret", account.secret)
                put("digits", account.digits)
                put("period", account.period)
                put("algorithm", account.algorithm)
            }
            jsonArray.put(obj)
        }
        sharedPreferences.edit().putString("accounts", jsonArray.toString()).apply()
    }
}