package com.techne.vaultshield.data.audit

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.techne.vaultshield.domain.audit.AuditLog
import com.techne.vaultshield.domain.audit.AuditLogRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class AuditLogRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AuditLogRepository {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_audit_logs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _logs = MutableStateFlow<List<AuditLog>>(emptyList())

    init {
        loadLogs()
    }

    override fun getLogs(): Flow<List<AuditLog>> = _logs.asStateFlow()

    override suspend fun logAction(action: String, details: String) {
        val newLog = AuditLog(System.currentTimeMillis(), action, details)
        val current = _logs.value.toMutableList()
        current.add(0, newLog) // Add to top
        if (current.size > 100) current.removeAt(current.size - 1) // Keep last 100
        saveLogs(current)
        _logs.value = current
    }

    private fun loadLogs() {
        val jsonString = sharedPreferences.getString("logs", "[]") ?: "[]"
        val jsonArray = JSONArray(jsonString)
        val list = mutableListOf<AuditLog>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                AuditLog(
                    timestamp = obj.getLong("timestamp"),
                    action = obj.getString("action"),
                    details = obj.getString("details")
                )
            )
        }
        _logs.value = list
    }

    private fun saveLogs(logs: List<AuditLog>) {
        val jsonArray = JSONArray()
        logs.forEach { log ->
            val obj = JSONObject().apply {
                put("timestamp", log.timestamp)
                put("action", log.action)
                put("details", log.details)
            }
            jsonArray.put(obj)
        }
        sharedPreferences.edit { putString("logs", jsonArray.toString()) }
    }
}
