package com.techne.vaultshield.domain.audit

data class AuditLog(
    val timestamp: Long,
    val action: String,
    val details: String
)
