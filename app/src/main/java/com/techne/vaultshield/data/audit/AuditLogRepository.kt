package com.techne.vaultshield.data.audit

import com.techne.vaultshield.domain.audit.AuditLog
import kotlinx.coroutines.flow.Flow

interface AuditLogRepository {
    fun getLogs(): Flow<List<AuditLog>>
    suspend fun logAction(action: String, details: String)
}
