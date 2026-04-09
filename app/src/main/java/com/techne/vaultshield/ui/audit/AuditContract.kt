package com.techne.vaultshield.ui.audit

import com.techne.vaultshield.domain.audit.AuditLog

data class AuditState(
    val auditLogs: List<AuditLog> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class AuditIntent {
    object LoadAuditLogs : AuditIntent()
}
