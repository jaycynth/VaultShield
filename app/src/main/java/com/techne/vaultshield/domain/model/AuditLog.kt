package com.techne.vaultshield.domain.model

data class AuditLog(
    val timestamp: Long,
    val action: String,
    val details: String
)
