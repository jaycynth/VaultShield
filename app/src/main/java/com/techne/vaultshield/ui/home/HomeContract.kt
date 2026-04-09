package com.techne.vaultshield.ui.home

import com.techne.vaultshield.domain.model.OtpAccount
import com.techne.vaultshield.security.SecurityManager

data class HomeState(
    val accounts: List<OtpAccount> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeviceSecure: Boolean = true,
    val integrityResult: SecurityManager.IntegrityResult? = null,
    val isAuthenticated: Boolean = false,
    val isAuditVisible: Boolean = false
)

sealed class HomeIntent {
    object LoadAccounts : HomeIntent()
    data class DeleteAccount(val id: String) : HomeIntent()
    data class RevealAccount(val id: String) : HomeIntent()
    data class CopyOtp(val account: OtpAccount) : HomeIntent()
    object Authenticate : HomeIntent()
    object AddAccount : HomeIntent()
    object LockVault : HomeIntent()
    object ToggleAudit : HomeIntent()
    object CreateBackup : HomeIntent()
    data class RestoreBackup(val encryptedData: String) : HomeIntent()
}
