package com.techne.vaultshield.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techne.vaultshield.domain.model.OtpRepository
import com.techne.vaultshield.domain.audit.AuditLogRepository
import com.techne.vaultshield.domain.model.OtpAccount
import com.techne.vaultshield.security.BackupManager
import com.techne.vaultshield.security.SecureClipboardManager
import com.techne.vaultshield.security.SecurityManager
import com.techne.vaultshield.security.TotpGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: OtpRepository,
    private val securityManager: SecurityManager,
    private val auditLogRepository: AuditLogRepository,
    private val clipboardManager: SecureClipboardManager,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var sessionTimeoutJob: Job? = null
    private val SESSION_TIMEOUT_MS = 5 * 60 * 1000L // 5 minutes

    init {
        checkSecurity()
        loadAccounts()
    }

    private fun checkSecurity() {
        val result = securityManager.checkEnvironmentIntegrity()
        _state.update { 
            it.copy(
                isDeviceSecure = result.isPass, 
                integrityResult = result,
                error = if (!result.isPass) "Security Integrity Warning" else null
            ) 
        }
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            repository.getAccounts().collect { accounts ->
                _state.update { it.copy(accounts = accounts) }
            }
        }
    }

    fun handleIntent(intent: HomeIntent) {
        resetSessionTimeout()
        when (intent) {
            is HomeIntent.LoadAccounts -> loadAccounts()
            is HomeIntent.DeleteAccount -> {
                viewModelScope.launch {
                    repository.deleteAccount(intent.id)
                    auditLogRepository.logAction("DELETE_ACCOUNT", "Account ID: ${intent.id}")
                }
            }
            is HomeIntent.RevealAccount -> {
                val updatedAccounts = _state.value.accounts.map {
                    if (it.id == intent.id) it.copy(isRevealed = true) else it
                }
                _state.update { it.copy(accounts = updatedAccounts) }
                viewModelScope.launch {
                    auditLogRepository.logAction("REVEAL_ACCOUNT", "Account ID: ${intent.id}")
                }
            }
            is HomeIntent.Authenticate -> {
                _state.update { it.copy(isAuthenticated = true) }
                viewModelScope.launch {
                    auditLogRepository.logAction("AUTHENTICATE_SUCCESS", "User unlocked the vault")
                }
            }
            is HomeIntent.AddAccount -> {
                viewModelScope.launch {
                    val newAccount = OtpAccount(
                        id = UUID.randomUUID().toString(),
                        issuer = "VaultShield",
                        accountName = "demo@vaultshield.com",
                        secret = "JBSWY3DPEHPK3PXP"
                    )
                    repository.addAccount(newAccount)
                    auditLogRepository.logAction("ADD_ACCOUNT", "Issuer: ${newAccount.issuer}")
                }
            }
            is HomeIntent.CopyOtp -> {
                val otp = TotpGenerator.generateTotp(intent.account.secret)
                clipboardManager.copyToClipboard("OTP Code", otp)
                viewModelScope.launch {
                    auditLogRepository.logAction("COPY_OTP", "Account: ${intent.account.issuer}")
                }
            }
            is HomeIntent.LockVault -> {
                _state.update { 
                    it.copy(
                        isAuthenticated = false,
                        isAuditVisible = false,
                        accounts = it.accounts.map { acc -> acc.copy(isRevealed = false) }
                    ) 
                }
            }
            is HomeIntent.ToggleAudit -> {
                _state.update { it.copy(isAuditVisible = !it.isAuditVisible) }
            }
            is HomeIntent.CreateBackup -> {
                viewModelScope.launch {
                    val backupData = backupManager.createEncryptedBackup("secure-backup-pass".toCharArray())
                    auditLogRepository.logAction("EXPORT_BACKUP", "Encrypted backup created")
                    _state.update { it.copy(error = "Backup Created") }
                }
            }
            is HomeIntent.RestoreBackup -> {
                viewModelScope.launch {
                    val result = backupManager.restoreFromBackup(intent.encryptedData, "secure-backup-pass".toCharArray())
                    if (result.isSuccess) {
                        auditLogRepository.logAction("IMPORT_BACKUP", "Successfully restored ${result.getOrNull()} accounts")
                    } else {
                        _state.update { it.copy(error = "Restore failed: Invalid password or data") }
                    }
                }
            }
        }
    }

    private fun resetSessionTimeout() {
        sessionTimeoutJob?.cancel()
        sessionTimeoutJob = viewModelScope.launch {
            delay(SESSION_TIMEOUT_MS)
            handleIntent(HomeIntent.LockVault)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sessionTimeoutJob?.cancel()
    }
}
