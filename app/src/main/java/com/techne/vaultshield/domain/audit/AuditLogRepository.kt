package com.techne.vaultshield.domain.audit

import kotlinx.coroutines.flow.Flow

interface AuditLogRepository {
    fun getLogs(): Flow<List<AuditLog>>
    suspend fun logAction(action: String, details: String)
}
